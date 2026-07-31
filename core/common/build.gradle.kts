plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.core.common"
}

dependencies {
  implementation(libs.androidx.core.ktx)
}