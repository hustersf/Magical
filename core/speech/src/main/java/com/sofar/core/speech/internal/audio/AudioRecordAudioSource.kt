package com.sofar.core.speech.internal.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

internal class AudioRecordAudioSource(
  context: Context,
  private val config: AudioRecordConfig = AudioRecordConfig(),
) : AudioSource {

  private val appContext = context.applicationContext
  private var audioRecord: AudioRecord? = null
  @Volatile private var recording: Boolean = false

  @SuppressLint("MissingPermission")
  override fun start(): Flow<AudioFrame> = callbackFlow {
    if (!AudioPermissionChecker.hasRecordAudioPermission(appContext)) {
      close(SecurityException("RECORD_AUDIO permission is not granted"))
      return@callbackFlow
    }

    val minBufferSize = AudioRecord.getMinBufferSize(
      config.sampleRate,
      config.channelConfig,
      config.audioFormat,
    )
    if (minBufferSize <= 0) {
      close(IllegalStateException("Invalid AudioRecord minBufferSize=$minBufferSize"))
      return@callbackFlow
    }

    val bufferSize = minBufferSize * config.bufferSizeFactor.coerceAtLeast(1)
    val record = AudioRecord(
      MediaRecorder.AudioSource.VOICE_RECOGNITION,
      config.sampleRate,
      config.channelConfig,
      config.audioFormat,
      bufferSize,
    )
    if (record.state != AudioRecord.STATE_INITIALIZED) {
      record.release()
      close(IllegalStateException("AudioRecord initialize failed"))
      return@callbackFlow
    }

    audioRecord = record
    recording = true
    record.startRecording()

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val sampleBuffer = ShortArray(bufferSize / BYTES_PER_SAMPLE)
    scope.launch {
      while (recording) {
        val readSize = record.read(sampleBuffer, 0, sampleBuffer.size)
        if (readSize > 0) {
          val samples = FloatArray(readSize)
          var sumSquare = 0.0
          for (index in 0 until readSize) {
            val sample = sampleBuffer[index] / PCM_16_MAX_VALUE
            samples[index] = sample
            sumSquare += sample * sample
          }
          trySend(
            AudioFrame(
              samples = samples,
              sampleRate = config.sampleRate,
              rmsDb = calculateRmsDb(sumSquare, readSize),
              timestampMillis = System.currentTimeMillis(),
            )
          )
        } else if (readSize < 0) {
          Log.w(TAG, "AudioRecord.read failed: $readSize")
        }
      }
    }

    awaitClose {
      recording = false
      scope.cancel()
      stopAndRelease(record)
      if (audioRecord === record) {
        audioRecord = null
      }
    }
  }

  override fun stop() {
    recording = false
    audioRecord?.let(::stopAndRelease)
    audioRecord = null
  }

  override fun release() {
    stop()
  }

  private fun stopAndRelease(record: AudioRecord) {
    runCatching {
      if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
        record.stop()
      }
    }
    runCatching { record.release() }
  }

  private fun calculateRmsDb(sumSquare: Double, size: Int): Float {
    if (size <= 0 || sumSquare <= 0.0) return MIN_RMS_DB
    val rms = sqrt(sumSquare / size)
    return (20.0 * log10(rms.coerceAtLeast(1.0e-9))).toFloat()
  }

  private companion object {
    private const val TAG = "AudioRecordAudioSource"
    private const val BYTES_PER_SAMPLE = 2
    private const val PCM_16_MAX_VALUE = 32768f
    private const val MIN_RMS_DB = -90f
  }
}