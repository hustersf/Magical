plugins {
  alias(libs.plugins.convention.android.library)
}

android {
  namespace = "com.sofar.core.media"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.lifecycle.runtime.ktx)
}