plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.network"
}

dependencies {
  implementation(libs.retrofit)
  implementation(libs.retrofit.serialization)
  implementation(libs.retrofit.result)
  implementation(libs.retrofit.gson)
  implementation(libs.retrofit.rxjava2)
  implementation(libs.retrofit.scalars)
  implementation(libs.okhttp)
  implementation(libs.okhttp.logging)
}