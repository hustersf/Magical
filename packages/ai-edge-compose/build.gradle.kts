plugins {
  alias(libs.plugins.convention.android.application)
  alias(libs.plugins.convention.android.application.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.sofar.ai.edge.compose"

  defaultConfig {
    applicationId = "com.sofar.ai.edge.compose"
    versionCode = 1
    versionName = "1.0"
  }
}

dependencies {
  implementation(project(":core:ai-edge:design-compose"))
  implementation(project(":core:ai-edge:navigation"))
  implementation(project(":core:ai-edge:domain"))
  implementation(project(":core:res:icon"))

  implementation(project(":feature:ai-edge:chat:api"))
  implementation(project(":feature:ai-edge:chat:impl-compose"))
  implementation(project(":feature:ai-edge:agent:api"))
  implementation(project(":feature:ai-edge:agent:impl-compose"))
  implementation(project(":feature:ai-edge:explore:api"))
  implementation(project(":feature:ai-edge:explore:impl-compose"))
  implementation(project(":feature:ai-edge:models:api"))
  implementation(project(":feature:ai-edge:models:impl-compose"))

  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.kotlinx.serialization)
  implementation(libs.androidx.compose.material.icons.extended)

  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
}
