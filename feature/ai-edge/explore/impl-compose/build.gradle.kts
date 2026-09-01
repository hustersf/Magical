plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.convention.android.library.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.feature.ai.edge.explore.impl"
}

dependencies {
  implementation(project(":feature:ai-edge:explore:api"))

  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.material3)

}