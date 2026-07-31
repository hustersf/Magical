plugins {
    alias(libs.plugins.convention.android.application)
}

android {
    namespace = "com.sofar.snapu"

    defaultConfig {
        applicationId = "com.sofar.snapu"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":core:base"))
    implementation(project(":core:mlkit"))
    implementation(project(":core:auto-play"))
    implementation(project(":framework:utility"))
    implementation(project(":framework:widget"))
    implementation(project(":framework:network"))
    implementation(project(":framework:image"))
    implementation(project(":framework:player"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.fresco)
    implementation(libs.glide)

    implementation(libs.media3.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
