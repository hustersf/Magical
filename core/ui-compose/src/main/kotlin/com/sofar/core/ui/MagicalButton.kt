package com.sofar.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 💡 正统底座规范：语义化消费示例
 * 该组件不直接硬编码颜色或尺寸，而是通过 MaterialTheme.colorScheme 槽位获取品牌色。
 */
@Composable
fun MagicalButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Button(
    onClick = onClick,
    modifier = modifier.padding(8.dp),
    shape = MaterialTheme.shapes.medium
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge
    )
  }
}