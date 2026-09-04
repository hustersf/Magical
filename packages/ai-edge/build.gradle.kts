plugins {
  alias(libs.plugins.convention.android.application)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.sofar.ai.edge"

  defaultConfig {
    applicationId = "com.sofar.ai.edge"
    versionCode = 1
    versionName = "1.0"
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
  implementation(project(":core:res:icon"))
  implementation(project(":core:ai-edge:design"))
  implementation(project(":core:ai-edge:data"))
  implementation(project(":core:ai-edge:domain"))

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
  implementation(project(":feature:ai-edge:explore:api"))
  implementation(project(":feature:ai-edge:explore:impl"))
}