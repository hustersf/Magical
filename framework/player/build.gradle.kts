plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.sofar.player"
}

dependencies {
    implementation(libs.media3.common)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
}