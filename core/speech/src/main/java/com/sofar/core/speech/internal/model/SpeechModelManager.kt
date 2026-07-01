package com.sofar.core.speech.internal.model

import android.content.Context
import java.io.File

internal class SpeechModelManager(
  context: Context,
  models: List<SpeechModel> = emptyList(),
) {
  private val modelMap = models.associateBy { it.id }.toMutableMap()
  private val pathResolver = ModelPathResolver()
  private val validator = ModelValidator(context, pathResolver)

  @Suppress("unused")
  fun register(model: SpeechModel) {
    modelMap[model.id] = model
  }

  @Suppress("unused")
  fun unregister(modelId: String) {
    modelMap.remove(modelId)
  }

  fun get(modelId: String): SpeechModel? = modelMap[modelId]

  fun require(modelId: String): SpeechModel {
    return get(modelId) ?: error("Speech model is not registered: $modelId")
  }

  fun resolveModelDir(model: SpeechModel): String {
    return pathResolver.resolveModelDir(model.source)
  }

  fun validate(model: SpeechModel): ModelValidationResult {
    return validator.validate(model)
  }

  fun hasModels(): Boolean = modelMap.isNotEmpty()
}

internal class ModelPathResolver(
) {
  fun resolveModelDir(source: SpeechModelSource): String {
    return when (source) {
      is SpeechModelSource.Assets -> source.assetDir.trimEnd('/')
      is SpeechModelSource.LocalFile -> source.dir
      is SpeechModelSource.Downloaded -> source.dir
    }
  }

  fun resolveFile(source: SpeechModelSource, relativePath: String): String {
    val normalized = relativePath.trimStart('/')
    return when (source) {
      is SpeechModelSource.Assets -> "${source.assetDir.trimEnd('/')}/$normalized"
      is SpeechModelSource.LocalFile -> File(source.dir, normalized).absolutePath
      is SpeechModelSource.Downloaded -> File(source.dir, normalized).absolutePath
    }
  }

  fun isAssets(source: SpeechModelSource): Boolean = source is SpeechModelSource.Assets
}

internal class ModelValidator(
  private val context: Context,
  private val pathResolver: ModelPathResolver = ModelPathResolver(),
) {
  fun validate(model: SpeechModel): ModelValidationResult {
    val missingFiles = model.sherpaConfig.requiredFiles().filterNot { exists(model.source, it) }
    return ModelValidationResult(
      isValid = missingFiles.isEmpty(),
      missingFiles = missingFiles,
      message = if (missingFiles.isEmpty()) null else "Missing model files: ${missingFiles.joinToString()}",
    )
  }

  private fun exists(source: SpeechModelSource, relativePath: String): Boolean {
    return if (pathResolver.isAssets(source)) {
      runCatching {
        context.assets.open(pathResolver.resolveFile(source, relativePath)).close()
        true
      }.getOrDefault(false)
    } else {
      File(pathResolver.resolveFile(source, relativePath)).exists()
    }
  }
}

@Suppress("unused")
internal class AssetModelInstaller(
  private val context: Context,
) {
  fun install(assetDir: String, targetDir: File) {
    copyAssetDir(assetDir.trimEnd('/'), targetDir)
  }

  private fun copyAssetDir(assetDir: String, targetDir: File) {
    targetDir.mkdirs()
    val children = context.assets.list(assetDir).orEmpty()
    if (children.isEmpty()) {
      context.assets.open(assetDir).use { input ->
        targetDir.outputStream().use { output -> input.copyTo(output) }
      }
      return
    }
    children.forEach { child ->
      val childAsset = "$assetDir/$child"
      val childTarget = File(targetDir, child)
      val grandChildren = context.assets.list(childAsset).orEmpty()
      if (grandChildren.isEmpty()) {
        context.assets.open(childAsset).use { input ->
          childTarget.outputStream().use { output -> input.copyTo(output) }
        }
      } else {
        copyAssetDir(childAsset, childTarget)
      }
    }
  }
}