plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.sofar.social.sdk"
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}




