package com.dailyquestion.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import com.dailyquestion.ui.theme.ProgressDotDone
import com.dailyquestion.ui.theme.ProgressDotEmpty
import com.dailyquestion.ui.theme.ProgressDotEmptyDark
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * 年度进度矩阵 — 横竖方向已交换
 * 73行纵排 × 5列横排，填充方向先下后右
 */
@Composable
fun YearProgressGrid(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    val today = LocalDate.now()
    val dayOfYear = today.dayOfYear
    val daysInYear = today.lengthOfYear()
    val progress = dayOfYear.toFloat() / daysInYear
    val weekOfYear = today.get(WeekFields.of(Locale.getDefault()).weekOfYear())
    val totalWeeks = 52

    val doneColor = ProgressDotDone
    val emptyColor = if (isDarkMode) ProgressDotEmptyDark else ProgressDotEmpty
    val progressColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        // 标题行
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("2026 进度", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${String.format("%.1f", progress * 100)}%", style = MaterialTheme.typography.labelLarge, color = progressColor)
        }

        Spacer(Modifier.height(6.dp))

        // 进度条
        Box(Modifier.fillMaxWidth().height(4.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawRoundRect(color = emptyColor, size = size, cornerRadius = CornerRadius(2.dp.toPx()))
                drawRoundRect(color = doneColor, size = Size(size.width * progress, size.height), cornerRadius = CornerRadius(2.dp.toPx()))
            }
        }

        Spacer(Modifier.height(3.dp))
        Text("第 $dayOfYear / $daysInYear 天 · 第 $weekOfYear / $totalWeeks 周",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))

        Spacer(Modifier.height(10.dp))

        // 5行 × 73列，填充方向：先下后右
        val rows = 5
        val columns = 73
        val cellGap = 3.dp

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height((8.dp + cellGap) * rows)
        ) {
            val gapPx = cellGap.toPx()
            // 计算每个格子宽度以填满屏幕
            val cellW = (size.width - gapPx * (columns - 1).toFloat()) / columns.toFloat()
            val cellH = 8.dp.toPx()

            for (i in 0 until daysInYear) {
                val col = i / rows          // 先下后右
                val row = i % rows
                val x = col.toFloat() * (cellW + gapPx)
                val y = row.toFloat() * (cellH + gapPx)
                val isDone = i < dayOfYear
                drawRoundRect(
                    color = if (isDone) doneColor.copy(alpha = 0.8f) else emptyColor,
                    topLeft = Offset(x, y),
                    size = Size(cellW, cellH),
                    cornerRadius = CornerRadius(1.5.dp.toPx())
                )
            }
        }
    }
}
