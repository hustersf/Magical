plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.core.network"
}

dependencies {
  implementation(libs.retrofit)
  implementation(libs.retrofit.serialization)
  implementation(libs.retrofit.result)
  implementation(libs.okhttp)
  implementation(libs.okhttp.logging)
}