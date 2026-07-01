package com.sofar.core.speech.internal.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow

internal interface AudioSource {
  fun start(): Flow<AudioFrame>
  fun stop()
  fun release()
}

internal data class AudioFrame(
  val samples: FloatArray,
  val sampleRate: Int,
  val rmsDb: Float,
  val timestampMillis: Long,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AudioFrame) return false
    return samples.contentEquals(other.samples) &&
        sampleRate == other.sampleRate &&
        rmsDb == other.rmsDb &&
        timestampMillis == other.timestampMillis
  }

  override fun hashCode(): Int {
    var result = samples.contentHashCode()
    result = 31 * result + sampleRate
    result = 31 * result + rmsDb.hashCode()
    result = 31 * result + timestampMillis.hashCode()
    return result
  }
}

internal data class AudioRecordConfig(
  val sampleRate: Int = 16_000,
  val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
  val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
  val bufferSizeFactor: Int = 2,
)

internal object AudioPermissionChecker {
  fun hasRecordAudioPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
  }
}