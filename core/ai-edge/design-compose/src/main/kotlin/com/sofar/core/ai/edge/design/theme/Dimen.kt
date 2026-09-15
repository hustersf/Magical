package com.sofar.core.ai.edge.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 🪐 1. 基础网格间距系统
 */
@Immutable
data class Spacing(
  val xs: Dp = 4.dp,
  val sm: Dp = 8.dp,
  val md: Dp = 12.dp,
  val lg: Dp = 16.dp,
  val xl: Dp = 24.dp,
  val xxl: Dp = 32.dp
)

/**
 * 📐 2. 组件圆角规范
 */
@Immutable
data class Corners(
  val xs: Dp = 4.dp,
  val md: Dp = 12.dp,
  val lg: Dp = 16.dp,
  val pill: Dp = 999.dp
)

/**
 * 📦 3. 卡片与布局物理常量
 */
@Immutable
data class Elevations(
  val card: Dp = 2.dp,
  val overlay: Dp = 6.dp,
  val stroke: Dp = 0.5.dp,
  val trackThickness: Dp = 5.dp
)

/**
 * 🎨 4. 图标与头像视窗尺寸规范
 */
@Immutable
data class IconSizes(
  val xs: Dp = 12.dp,
  val inlineStatus: Dp = 16.dp,
  val small: Dp = 16.dp,
  val medium: Dp = 24.dp,
  val large: Dp = 32.dp,
  val xxl: Dp = 64.dp
)

/**
 * ⚡ 5. 按钮与输入框尺寸规范
 */
@Immutable
data class WidgetSizes(
  val buttonHeightSmall: Dp = 36.dp,
  val buttonHeightStandard: Dp = 48.dp,
  val touchTargetIcon: Dp = 40.dp,
  val touchTargetPadding: Dp = 8.dp,
  val toolbarHeightStandard: Dp = 56.dp,
  val toolbarHeightLarge: Dp = 64.dp,
  val inputFieldMinHeight: Dp = 50.dp
)

// 定义 CompositionLocal
val LocalSpacing = staticCompositionLocalOf { Spacing() }
val LocalCorners = staticCompositionLocalOf { Corners() }
val LocalElevations = staticCompositionLocalOf { Elevations() }
val LocalIconSizes = staticCompositionLocalOf { IconSizes() }
val LocalWidgetSizes = staticCompositionLocalOf { WidgetSizes() }
