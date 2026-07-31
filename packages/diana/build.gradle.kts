plugins {
    alias(libs.plugins.convention.android.application)
}

android {
    namespace = "com.sofar.diana"

    defaultConfig {
        applicationId = "com.sofar.diana"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
