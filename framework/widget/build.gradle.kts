plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.sofar.widget"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
}




