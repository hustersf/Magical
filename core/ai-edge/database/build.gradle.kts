plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.core.ai.edge.database"
}

dependencies {
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
}