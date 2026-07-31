plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.core.speech"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.coroutines.android)
  implementation(libs.coroutines)
  implementation(project(":core:download"))
}