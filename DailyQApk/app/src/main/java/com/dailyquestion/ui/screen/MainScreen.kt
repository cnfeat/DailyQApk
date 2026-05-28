package com.dailyquestion.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailyquestion.DailyQuestionWorker
import com.dailyquestion.WidgetUpdater
import com.dailyquestion.model.QuestionManager
import com.dailyquestion.ui.component.DotIndicator
import com.dailyquestion.ui.component.QuestionContent
import com.dailyquestion.ui.component.SettingsSheet
import com.dailyquestion.ui.component.ShareSheet
import com.dailyquestion.ui.theme.*
import com.dailyquestion.ui.util.HapticUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    questionManager: QuestionManager,
    context: android.content.Context = LocalContext.current
) {
    var currentQuestion by remember { mutableStateOf(questionManager.getTodayQuestion()) }
    var currentIndex by remember { mutableStateOf(questionManager.getCurrentProgress().first) }
    val totalCount = 3
    var showSettings by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dateStr = LocalDate.now().let {
        "${it.monthValue}月${it.dayOfMonth}日 ${it.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE)}"
    }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var startupDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        DailyQuestionWorker.scheduleDailyUpdate(context)
        kotlinx.coroutines.delay(200)
        startupDone = true
    }

    // 深色呼吸光效
    val breathAlpha by rememberInfiniteTransition(label = "bg_breath").animateFloat(
        initialValue = 0.0f,
        targetValue = if (isDark) 0.06f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "更多",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(
                    if (isDark) listOf(PageBgGradientStartDark, PageBgGradientEndDark)
                    else listOf(PageBgGradientStart, PageBgGradientEnd)
                ))
                .then(
                    if (isDark) Modifier.background(Brush.verticalGradient(
                        0.0f to PageBgGradientStartDark,
                        1.0f to Color.White.copy(alpha = breathAlpha)
                    )) else Modifier
                )
        ) {
            AnimatedVisibility(
                visible = startupDone,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(12.dp))

                    // 顶部：日期行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            dateStr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 问题卡片
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp, vertical = 28.dp)
                        ) {
                            // 今日问题 标签
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(0.5f))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "今日问题",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            // 可滚动内容
                            Column(
                                Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                AnimatedContent(
                                    targetState = currentQuestion.id,
                                    transitionSpec = {
                                        slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(350)) togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(350))
                                    },
                                    label = "question_card"
                                ) { _ ->
                                    QuestionContent(
                                        question = currentQuestion,
                                        primaryColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 操作栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val next = questionManager.switchToNext()
                                currentQuestion = next
                                currentIndex = questionManager.getCurrentProgress().first
                                scope.launch { withContext(Dispatchers.IO) { WidgetUpdater.refreshAll(context) } }
                                HapticUtil.lightTap(context)
                            },
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            DotIndicator(
                                currentIndex = currentIndex,
                                totalCount = totalCount,
                                activeColor = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("换一问", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                        }

                        Spacer(Modifier.width(12.dp))

                        FilledTonalIconButton(
                            onClick = { showShare = true },
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(0.12f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.MoreHoriz, contentDescription = "分享", modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 年进度
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        CircularYearProgress(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            isDarkMode = isDark
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    // 分享弹窗
    if (showShare) {
        ShareSheet(
            question = currentQuestion,
            onDismiss = { showShare = false },
            scope = scope
        )
    }

    // 设置面板
    if (showSettings) {
        SettingsSheet(onDismiss = { showSettings = false })
    }
}
