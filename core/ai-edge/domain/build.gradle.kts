plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.core.ai.edge.domain"
}

dependencies {
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  implementation(project(":core:ai-edge:data"))
}