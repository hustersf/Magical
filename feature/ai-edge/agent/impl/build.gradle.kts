plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.feature.ai.edge.agent.impl"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin {
    jvmToolchain(17)
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.constraintlayout)

  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.material)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  implementation(project(":feature:ai-edge:chat:api"))
  implementation(project(":core:res"))
  implementation(project(":core:ui"))
  implementation(project(":core:common"))
  implementation(project(":core:ai-edge:data"))
}