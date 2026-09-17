plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.feature.ai.edge.chat.logic"
}

dependencies {
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  implementation(project(":feature:ai-edge:chat:api"))
  implementation(project(":core:common"))
  implementation(project(":core:ai-edge:data"))
  implementation(project(":core:ai-edge:domain"))
}