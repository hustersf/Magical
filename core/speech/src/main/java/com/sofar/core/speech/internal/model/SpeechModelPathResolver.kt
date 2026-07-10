package com.sofar.core.speech.internal.model

import java.io.File

internal class SpeechModelPathResolver {
  fun resolveModelDir(model: SpeechModel): String {
    return model.localDir
  }

  fun resolveFile(model: SpeechModel, relativePath: String): String {
    val normalized = relativePath.trimStart('/')
    return File(model.localDir, normalized).absolutePath
  }
}

internal class SpeechModelValidator(
  private val pathResolver: SpeechModelPathResolver = SpeechModelPathResolver(),
) {
  fun validate(model: SpeechModel): ModelValidationResult {
    val missingFiles = model.sherpaConfig.requiredFiles().filterNot { exists(model, it) }
    return ModelValidationResult(
      isValid = missingFiles.isEmpty(),
      missingFiles = missingFiles,
      message = if (missingFiles.isEmpty()) null else "Missing model files: ${missingFiles.joinToString()}",
    )
  }

  private fun exists(model: SpeechModel, relativePath: String): Boolean {
    return File(pathResolver.resolveFile(model, relativePath)).exists()
  }
}
