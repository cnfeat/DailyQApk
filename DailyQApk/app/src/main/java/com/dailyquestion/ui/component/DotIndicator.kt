package com.dailyquestion.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 圆点进度指示器：始终显示 3 个等大圆点
 * @param currentIndex 当前题号 (0-based)
 * @param totalCount 总题数
 * @param activeColor 圆点颜色
 */
@Composable
fun DotIndicator(
    currentIndex: Int,
    totalCount: Int,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalCount) {
            val dotColor = when {
                i < currentIndex -> activeColor.copy(0.35f) // 已看：半透明
                i == currentIndex -> activeColor               // 当前：实心
                else -> activeColor.copy(0.15f)                // 未看：极浅
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}
