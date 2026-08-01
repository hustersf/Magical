plugins {
    alias(libs.plugins.convention.android.application)
}

android {
    namespace = "com.sofar.apollo"

    defaultConfig {
        applicationId = "com.sofar.apollo"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":core:base"))
    implementation(project(":core:login"))
    implementation(project(":core:share"))

    implementation(project(":framework:utility"))
    implementation(project(":framework:widget"))
    implementation(project(":framework:network"))
    implementation(project(":framework:player"))
    implementation(project(":framework:image"))
    implementation(project(":framework:skin"))

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.fresco)

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
