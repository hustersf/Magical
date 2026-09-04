plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.feature.ai.edge.models.impl"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.coordinatorlayout)
  implementation(libs.material)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.kotlinx.serialization)
  implementation(libs.markwon)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  implementation(project(":feature:ai-edge:models:api"))
  implementation(project(":feature:ai-edge:chat:api"))
  implementation(project(":core:ui"))
  implementation(project(":core:res:icon"))
  implementation(project(":core:common"))
  implementation(project(":core:ai-edge:data"))
  implementation(project(":core:ai-edge:domain"))
}