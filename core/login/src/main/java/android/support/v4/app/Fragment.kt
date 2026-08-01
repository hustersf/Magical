package android.support.v4.app

/**
 * 专门为了让腾讯 QQ Open SDK 在 Gradle 9.x / 纯 AndroidX / 禁用 Jetifier
 * 环境下通过方法签名编译而设立的“防腐空壳类”。
 *
 * 作用：欺骗 Java 编译器通过编译，打包时 R8/ProGuard 会自动将其当做未使用的类彻底剔除。
 */
class Fragment {
  // 保持绝对真空，什么都不写
}