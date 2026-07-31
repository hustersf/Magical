plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.feature.ai.edge.chat.impl"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  implementation(libs.markwon)

  implementation(project(":feature:ai-edge:chat:api"))
  implementation(project(":core:res"))
  implementation(project(":core:ui"))
  implementation(project(":core:common"))
  implementation(project(":core:media"))
  implementation(project(":core:image"))
  implementation(project(":core:ai-edge:data"))
  implementation(project(":core:ai-edge:design"))
  implementation(project(":core:ai-edge:domain"))
  implementation(project(":core:speech"))
}