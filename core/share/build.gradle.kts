plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.sofar.share"
}

dependencies {
    implementation(project(":external:social-sdk"))
    implementation(project(":core:base"))
}




