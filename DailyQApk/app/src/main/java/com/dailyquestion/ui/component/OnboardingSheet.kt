package com.dailyquestion.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 日课一问 — 首次使用引导页
 *
 * 新用户首次打开 App 时展示产品理念和基本操作说明。
 * 滑动翻页，最后一页点击"开始"关闭。
 */
@Composable
fun OnboardingSheet(
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            title = "日课一问",
            subtitle = "每日一问，破局人生",
            body = "每天一个深度反思问题，帮你跳出惯性思维，直面真正重要的事。"
        ),
        OnboardingPage(
            title = "每日三省",
            subtitle = "每天 3 道题，循环浏览",
            body = "系统每天为你随机抽取 3 道问题。点击「换一问」切换，1→2→3 循环。想清楚了再点下一题。"
        ),
        OnboardingPage(
            title = "深度追问",
            subtitle = "拓展引导，层层深入",
            body = "每个问题下方有拓展文字，引导你从不同角度思考。不一定要写答案，想的过程本身就是启发。"
        ),
        OnboardingPage(
            title = "桌面部件 & 分享",
            subtitle = "随时可见，随时可记",
            body = "添加桌面小部件，主屏幕直接查看今日问题。遇到触动你的问题，一键生成卡片保存或分享给朋友。"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 页面指示器
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 10.dp else 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (i == step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.25f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // 当前页内容
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        pages[step].title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        pages[step].subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        pages[step].body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                }
            }

            Spacer(Modifier.height(64.dp))

            // 底部按钮
            if (step < pages.lastIndex) {
                Button(
                    onClick = { step++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("下一步")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("开始反思")
                }
            }

            if (step > 0) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { step-- }) {
                    Text("上一步")
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss) {
                Text(
                    "跳过引导",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                )
            }
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val body: String
)
