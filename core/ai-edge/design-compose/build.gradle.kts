plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.convention.android.library.compose)
}

android {
  namespace = "com.sofar.core.ai.edge.design"
}

dependencies {
  api(libs.androidx.compose.material3)
}
