package com.sofar.core.ai.edge.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable() () -> Unit
) {
  CompositionLocalProvider(
    LocalSpacing provides Spacing(),
    LocalCorners provides Corners(),
    LocalElevations provides Elevations(),
    LocalIconSizes provides IconSizes(),
    LocalWidgetSizes provides WidgetSizes()
  ) {
    BaseAppTheme(
      darkTheme = darkTheme,
      dynamicColor = dynamicColor,
      content = content
    )
  }
}

/**
 * 快捷访问维度的扩展属性
 */
val MaterialTheme.spacing: Spacing
  @Composable
  get() = LocalSpacing.current

val MaterialTheme.corners: Corners
  @Composable
  get() = LocalCorners.current

val MaterialTheme.elevations: Elevations
  @Composable
  get() = LocalElevations.current

val MaterialTheme.iconSizes: IconSizes
  @Composable
  get() = LocalIconSizes.current

val MaterialTheme.widgetSizes: WidgetSizes
  @Composable
  get() = LocalWidgetSizes.current
