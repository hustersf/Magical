plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.core.ui"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  api(libs.material)
}