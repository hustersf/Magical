plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.core.download"
}

dependencies {
  implementation(libs.androidx.work.runtime.ktx)
}