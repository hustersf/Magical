plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.convention.android.library.compose)
}

android {
  namespace = "com.sofar.feature.ai.edge.models.impl"
}

dependencies {
  implementation(project(":feature:ai-edge:models:api"))
  implementation(project(":feature:ai-edge:models:logic"))
  implementation(project(":feature:ai-edge:chat:api"))
  implementation(project(":core:res:icon"))
  implementation(project(":core:ai-edge:data"))

  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
  implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
  implementation(libs.markdown.compose)
  implementation(libs.markdown.compose.m3)
}
