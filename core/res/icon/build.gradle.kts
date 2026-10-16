plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.core.res.icon"
}

dependencies {
  implementation(libs.androidx.core.ktx)
}