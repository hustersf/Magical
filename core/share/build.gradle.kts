plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.sofar.share"
}

dependencies {
    implementation(project(":core:social-sdk"))
}




