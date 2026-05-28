package com.dailyquestion.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyquestion.ui.theme.ProgressDotDone
import com.dailyquestion.ui.theme.ProgressDotEmpty
import com.dailyquestion.ui.theme.ProgressDotEmptyDark
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 年度圆环进度指示器
 *
 * 圆环显示已过天数占比，右侧显示文字信息。
 * 一行紧凑布局，替代原来的 73×5 点阵矩阵。
 */
@Composable
fun CircularYearProgress(
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

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ===== 左侧圆环 =====
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                )
                val arcSize = Size(radius * 2, radius * 2)

                // 底色圆环
                drawArc(
                    color = emptyColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 进度弧
                drawArc(
                    color = doneColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 圆环中央百分比
            Text(
                "${(progress * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = doneColor
            )
        }

        Spacer(Modifier.width(16.dp))

        // ===== 右侧文字信息 =====
        Column(modifier = Modifier.weight(1f)) {
            // 标题行
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "2026 进度",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${String.format("%.1f", progress * 100)}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = doneColor
                )
            }

            Spacer(Modifier.height(4.dp))

            // 细进度条
            Box(Modifier.fillMaxWidth().height(3.dp)) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(
                        color = emptyColor,
                        size = Size(w, h),
                        cornerRadius = CornerRadius(1.5.dp.toPx())
                    )
                    drawRoundRect(
                        color = doneColor,
                        size = Size(w * progress, h),
                        cornerRadius = CornerRadius(1.5.dp.toPx())
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            // 天数 + 周数
            Text(
                "今天是第 $dayOfYear / $daysInYear 天 · 第 $weekOfYear / $totalWeeks 周",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
