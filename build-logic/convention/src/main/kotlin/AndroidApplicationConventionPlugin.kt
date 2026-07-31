import com.android.build.api.dsl.ApplicationExtension
import com.sofar.buildlogic.configureKotlinAndroid
import com.sofar.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

abstract class AndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      // 应用 Android Application 基础插件
      pluginManager.apply("com.android.application")

      extensions.configure<ApplicationExtension> {
        //复用现有的 Kotlin 编译器选项和工具链配置函数
        configureKotlinAndroid(this)

        defaultConfig {
          // 从 Project.libs 中动态拉取版本，100% 对齐你的 Version Catalog 用法
          val configTargetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
          targetSdk = configTargetSdk
          testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        // 统一注入你的全矩阵 APP 专属签名配置
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

        // 统一配置符合你刚才修改后的 AGP 9.x 安全标准的 R8 优化混淆
        buildTypes {
          getByName("debug") {
            signingConfig = signingConfigs.getByName("myConfig")
            isMinifyEnabled = false
            proguardFiles(
              getDefaultProguardFile("proguard-android-optimize.txt"),
              "proguard-rules.pro"
            )
          }
          getByName("release") {
            signingConfig = signingConfigs.getByName("myConfig")
            isMinifyEnabled = true // APP 壳工程开启真正的 R8 压缩优化与去死代码
            proguardFiles(
              getDefaultProguardFile("proguard-android-optimize.txt"),
              "proguard-rules.pro"
            )
          }
        }
      }
    }
  }
}
