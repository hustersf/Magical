plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.core.res"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
}