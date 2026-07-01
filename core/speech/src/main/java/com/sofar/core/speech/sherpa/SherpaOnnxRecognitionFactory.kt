
package com.sofar.core.speech.sherpa

import android.content.res.AssetManager
import java.lang.reflect.Method

internal interface SherpaOnnxRecognitionFactory {
  fun create(
    modelConfig: SherpaOnnxModelConfig,
    runtimeConfig: SherpaOnnxConfig,
    modelDir: String,
    useAssetManager: Boolean,
  ): SherpaOnnxRecognition
}

internal interface SherpaOnnxRecognition {
  fun acceptWaveform(sampleRate: Int, samples: FloatArray)
  fun decode()
  fun getText(): String
  fun isEndpoint(): Boolean
  fun reset()
  fun release()
}

/**
 * Reflection based adapter for sherpa-onnx Android AAR.
 *
 * It keeps core:speech compile-time independent from a concrete sherpa-onnx artifact version.
 * Once a sherpa-onnx version is fixed, this file can be replaced by a strong-typed adapter.
 */
internal class ReflectiveSherpaOnnxRecognitionFactory(
  private val assetManager: AssetManager,
) : SherpaOnnxRecognitionFactory {

  override fun create(
    modelConfig: SherpaOnnxModelConfig,
    runtimeConfig: SherpaOnnxConfig,
    modelDir: String,
    useAssetManager: Boolean,
  ): SherpaOnnxRecognition {
    val recognitionClass = findClass("OnlineRecognizer")
    val featureConfig = newConfig("FeatureConfig").apply {
      setProperty("sampleRate", runtimeConfig.sampleRate)
      setProperty("featureDim", runtimeConfig.featureDim)
    }
    val onlineModelConfig = newConfig("OnlineModelConfig").apply {
      setProperty("tokens", resolvePath(modelDir, modelConfig.tokens, useAssetManager))
      setProperty("numThreads", runtimeConfig.numThreads)
      setProperty("provider", runtimeConfig.provider)
      setProperty("debug", runtimeConfig.debug)
      setModelConfig(modelConfig, modelDir, useAssetManager)
    }
    val recognitionConfig = newConfig("OnlineRecognizerConfig").apply {
      setProperty("featConfig", featureConfig)
      setProperty("modelConfig", onlineModelConfig)
      setProperty("decodingMethod", runtimeConfig.decodingMethod)
      setProperty("enableEndpoint", runtimeConfig.enableEndpoint)
      setProperty("rule1MinTrailingSilence", runtimeConfig.rule1MinTrailingSilence)
      setProperty("rule2MinTrailingSilence", runtimeConfig.rule2MinTrailingSilence)
      setProperty("rule3MinUtteranceLength", runtimeConfig.rule3MinUtteranceLength)
    }

    val recognition = createRecognitionInstance(recognitionClass, recognitionConfig, useAssetManager)
    return ReflectiveSherpaOnnxRecognition(recognition)
  }

  private fun Any.setModelConfig(
    modelConfig: SherpaOnnxModelConfig,
    modelDir: String,
    useAssetManager: Boolean,
  ) {
    when (modelConfig.modelType) {
      SherpaOnnxModelType.Transducer -> {
        val transducer = newConfig("OnlineTransducerModelConfig").apply {
          setProperty("encoder", resolvePath(modelDir, modelConfig.encoder.orEmpty(), useAssetManager))
          setProperty("decoder", resolvePath(modelDir, modelConfig.decoder.orEmpty(), useAssetManager))
          setProperty("joiner", resolvePath(modelDir, modelConfig.joiner.orEmpty(), useAssetManager))
        }
        setProperty("transducer", transducer)
      }
      SherpaOnnxModelType.Paraformer -> {
        val paraformer = newConfig("OnlineParaformerModelConfig").apply {
          setProperty("model", resolvePath(modelDir, modelConfig.model.orEmpty(), useAssetManager))
          setProperty("encoder", resolvePath(modelDir, modelConfig.encoder.orEmpty(), useAssetManager))
          setProperty("decoder", resolvePath(modelDir, modelConfig.decoder.orEmpty(), useAssetManager))
        }
        setProperty("paraformer", paraformer)
      }
      SherpaOnnxModelType.Zipformer2Ctc -> {
        val ctc = newConfig("OnlineZipformer2CtcModelConfig").apply {
          setProperty("model", resolvePath(modelDir, modelConfig.model.orEmpty(), useAssetManager))
        }
        setProperty("zipformer2Ctc", ctc)
      }
      SherpaOnnxModelType.Ctc -> {
        val ctc = newConfigOrNull("OnlineCtcModelConfig") ?: newConfig("OnlineZipformer2CtcModelConfig")
        ctc.setProperty("model", resolvePath(modelDir, modelConfig.model.orEmpty(), useAssetManager))
        setProperty("ctc", ctc)
        setProperty("zipformer2Ctc", ctc)
      }
      SherpaOnnxModelType.Whisper -> {
        val whisper = newConfig("OnlineWhisperModelConfig").apply {
          setProperty("encoder", resolvePath(modelDir, modelConfig.encoder.orEmpty(), useAssetManager))
          setProperty("decoder", resolvePath(modelDir, modelConfig.decoder.orEmpty(), useAssetManager))
        }
        setProperty("whisper", whisper)
      }
    }
  }

  private fun createRecognitionInstance(
    recognitionClass: Class<*>,
    recognitionConfig: Any,
    useAssetManager: Boolean,
  ): Any {
    recognitionClass.constructors.firstOrNull { constructor ->
      val types = constructor.parameterTypes
      useAssetManager && types.size == 2 && AssetManager::class.java.isAssignableFrom(types[0]) && types[1].isAssignableFrom(recognitionConfig.javaClass)
    }?.let { return it.newInstance(assetManager, recognitionConfig) }

    recognitionClass.constructors.firstOrNull { constructor ->
      val types = constructor.parameterTypes
      types.size == 1 && types[0].isAssignableFrom(recognitionConfig.javaClass)
    }?.let { return it.newInstance(recognitionConfig) }

    error("Unsupported sherpa-onnx OnlineRecognizer constructor. Please provide a custom SherpaOnnxRecognitionFactory.")
  }

  private fun newConfig(simpleName: String): Any {
    return newConfigOrNull(simpleName)
      ?: error("sherpa-onnx config class not found: $PACKAGE_NAME.$simpleName")
  }

  private fun newConfigOrNull(simpleName: String): Any? {
    val clazz = runCatching { findClass(simpleName) }.getOrNull() ?: return null
    return clazz.constructors.firstOrNull { it.parameterTypes.isEmpty() }?.newInstance()
  }

  private fun findClass(simpleName: String): Class<*> {
    return Class.forName("$PACKAGE_NAME.$simpleName")
  }

  private fun Any.setProperty(name: String, value: Any?) {
    if (value == null) return
    val setterName = "set" + name.replaceFirstChar { it.uppercase() }
    val setter = javaClass.methods.firstOrNull { method ->
      method.name == setterName && method.parameterTypes.size == 1 && accepts(method.parameterTypes[0], value)
    }
    if (setter != null) {
      setter.invoke(this, value)
      return
    }

    val field = runCatching { javaClass.getDeclaredField(name) }.getOrNull()
    if (field != null) {
      field.isAccessible = true
      field.set(this, value)
    }
  }

  private fun accepts(type: Class<*>, value: Any): Boolean {
    if (type.isPrimitive) {
      return when (type) {
        Int::class.javaPrimitiveType -> value is Int
        java.lang.Boolean.TYPE -> value is Boolean
        java.lang.Float.TYPE -> value is Float
        java.lang.Double.TYPE -> value is Double
        java.lang.Long.TYPE -> value is Long
        else -> false
      }
    }
    return type.isAssignableFrom(value.javaClass)
  }

  private fun resolvePath(modelDir: String, relativePath: String, useAssetManager: Boolean): String {
    if (relativePath.isBlank()) return relativePath
    val normalizedModelDir = modelDir.trimEnd('/')
    val normalizedRelative = relativePath.trimStart('/')
    return if (useAssetManager) {
      "$normalizedModelDir/$normalizedRelative"
    } else {
      java.io.File(normalizedModelDir, normalizedRelative).absolutePath
    }
  }

  private companion object {
    private const val PACKAGE_NAME = "com.k2fsa.sherpa.onnx"
  }
}

private class ReflectiveSherpaOnnxRecognition(
  private val recognizer: Any,
) : SherpaOnnxRecognition {

  private val stream: Any = call("createStream") ?: error("sherpa-onnx createStream returned null")

  override fun acceptWaveform(sampleRate: Int, samples: FloatArray) {
    call("acceptWaveform", stream, sampleRate, samples)
  }

  override fun decode() {
    while ((call("isReady", stream) as? Boolean) == true) {
      call("decode", stream)
    }
  }

  override fun getText(): String {
    val result = call("getResult", stream) ?: return ""
    return result.readStringProperty("text") ?: result.toString()
  }

  override fun isEndpoint(): Boolean {
    return (call("isEndpoint", stream) as? Boolean) ?: false
  }

  override fun reset() {
    call("reset", stream)
  }

  override fun release() {
    runCatching { stream.callNoArg("release") }
    runCatching { call("release") }
  }

  private fun call(methodName: String, vararg args: Any): Any? {
    val method = recognizer.javaClass.methods.firstOrNull { it.matches(methodName, args) }
      ?: error("sherpa-onnx method not found: ${recognizer.javaClass.name}#$methodName(${args.size})")
    return method.invoke(recognizer, *args)
  }

  private fun Any.callNoArg(methodName: String): Any? {
    val method = javaClass.methods.firstOrNull { it.name == methodName && it.parameterTypes.isEmpty() }
      ?: return null
    return method.invoke(this)
  }

  private fun Any.readStringProperty(name: String): String? {
    val getterName = "get" + name.replaceFirstChar { it.uppercase() }
    javaClass.methods.firstOrNull { it.name == getterName && it.parameterTypes.isEmpty() }?.let {
      return it.invoke(this) as? String
    }
    runCatching {
      val field = javaClass.getDeclaredField(name)
      field.isAccessible = true
      return field.get(this) as? String
    }
    return null
  }

  private fun Method.matches(name: String, args: Array<out Any>): Boolean {
    if (this.name != name || parameterTypes.size != args.size) return false
    return parameterTypes.indices.all { index -> accepts(parameterTypes[index], args[index]) }
  }

  private fun accepts(type: Class<*>, value: Any): Boolean {
    if (type.isPrimitive) {
      return when (type) {
        Int::class.javaPrimitiveType -> value is Int
        java.lang.Float.TYPE -> value is Float
        java.lang.Boolean.TYPE -> value is Boolean
        else -> false
      }
    }
    return type.isAssignableFrom(value.javaClass)
  }
}