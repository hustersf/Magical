plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.sofar.login"
}

dependencies {
    implementation(project(":core:social-sdk"))
    implementation(project(":framework:network"))

    implementation(libs.androidx.appcompat)

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.retrofit)
}




