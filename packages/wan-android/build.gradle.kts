plugins {
    alias(libs.plugins.convention.android.application)
}

android {
    namespace = "com.sofar.wan.android"

    defaultConfig {
        applicationId = "com.sofar.wan.android"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":core:legacy"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":core:webview"))
    implementation(project(":framework:network"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.flexbox)
    implementation(libs.material)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.paging)

    implementation(libs.coroutines)
    implementation(libs.coroutines.android)

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.fresco)

    implementation(libs.glide)
    implementation(libs.lottie)

    debugImplementation(libs.leakcanary)
}
