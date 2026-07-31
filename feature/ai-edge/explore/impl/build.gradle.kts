plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.feature.ai.edge.explore.impl"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.constraintlayout)
}