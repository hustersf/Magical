plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.convention.android.library.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.feature.ai.edge.chat.impl"
}

dependencies {
  implementation(project(":feature:ai-edge:chat:api"))
  implementation(project(":feature:ai-edge:chat:logic"))
  implementation(project(":core:res:icon"))
  implementation(project(":core:ai-edge:data"))
  implementation(project(":core:ai-edge:design-compose"))
  implementation(project(":core:ai-edge:navigation"))
  implementation(project(":core:speech"))
  implementation(project(":core:media"))
  implementation(project(":framework:image"))

  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
  implementation(libs.markdown.compose)
  implementation(libs.markdown.compose.m3)
}