plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.sofar.core.legacy"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.retrofit.gson)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
}




