plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.convention.android.library.compose)
}

android {
  namespace = "com.sofar.core.ui"
}

dependencies {
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.material3)
}