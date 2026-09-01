plugins {
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.sofar.feature.ai.edge.agent.api"
}

dependencies {
  api(project(":core:ai-edge:navigation"))
}
