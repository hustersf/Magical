plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.core.ai.edge.data"
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.retrofit)
  implementation(libs.retrofit.serialization)
  implementation(libs.retrofit.result)
  implementation(libs.okhttp)
  implementation(libs.okhttp.logging)
  implementation(libs.coroutines.android)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  implementation(libs.litertlm)

  implementation(project(":core:common"))
  implementation(project(":core:network"))
  implementation(project(":core:download"))
  api(project(":core:ai-edge:database"))
}