plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.ai.edge"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.sofar.ai.edge"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // 签名信息配置
  signingConfigs {
    create("myConfig") {
      storeFile = rootProject.file("magical.keystore")
      storePassword = "123456"
      keyAlias = "sofar"
      keyPassword = "123456"
      enableV1Signing = true
      enableV2Signing = true
    }
  }

  buildTypes {
    debug {
      signingConfig = signingConfigs.getByName("myConfig")
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    release {
      signingConfig = signingConfigs.getByName("myConfig")
      isMinifyEnabled = true
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
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.splashscreen)
  implementation(libs.androidx.viewpager2)
  implementation(libs.material)
  implementation(libs.coroutines.android)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  implementation(project(":core:ui"))
  implementation(project(":core:res"))
  implementation(project(":core:ai-edge:design"))
  implementation(project(":core:ai-edge:data"))

  implementation(project(":feature:ai-edge:agent:api"))
  implementation(project(":feature:ai-edge:agent:impl"))
  implementation(project(":feature:ai-edge:chat:api"))
  implementation(project(":feature:ai-edge:chat:impl"))
  implementation(project(":feature:ai-edge:meeting:api"))
  implementation(project(":feature:ai-edge:meeting:impl"))
  implementation(project(":feature:ai-edge:vision:api"))
  implementation(project(":feature:ai-edge:vision:impl"))
  implementation(project(":feature:ai-edge:models:api"))
  implementation(project(":feature:ai-edge:models:impl"))
}