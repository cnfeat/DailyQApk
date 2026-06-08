package com.dailyquestion.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dailyquestion.DailyQuestionWorker
import com.dailyquestion.WidgetUpdater
import com.dailyquestion.model.QuestionManager
import com.dailyquestion.ui.component.DotIndicator
import com.dailyquestion.ui.component.OnboardingSheet
import com.dailyquestion.ui.component.QuestionContent
import com.dailyquestion.ui.component.SettingsSheet
import com.dailyquestion.ui.theme.*
import com.dailyquestion.ui.util.HapticUtil
import com.dailyquestion.ui.util.generateShareBitmap
import com.dailyquestion.ui.util.saveToGallery
import com.dailyquestion.ui.util.shareImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** 暗色模式选项 */
enum class DarkModeOption { SYSTEM, DARK, LIGHT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    questionManager: QuestionManager,
    context: Context = LocalContext.current
) {
    var currentQuestion by remember { mutableStateOf(questionManager.getTodayQuestion()) }
    var currentIndex by remember { mutableStateOf(questionManager.getCurrentProgress().first) }
    val totalCount = 3
    var showSettings by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var shareBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 暗色模式偏好（持久化）
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var showOnboarding by remember {
        val seen = prefs.getBoolean("has_seen_onboarding", false)
        mutableStateOf(!seen)
    }
    var darkModeOption by remember {
        mutableStateOf(DarkModeOption.valueOf(prefs.getString("dark_mode", "SYSTEM") ?: "SYSTEM"))
    }

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

    // 前台恢复时重新同步 Widget 换题后的状态
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentQuestion = questionManager.getTodayQuestion()
                currentIndex = questionManager.getCurrentProgress().first
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val breathAlpha by rememberInfiniteTransition(label = "bg_breath").animateFloat(
        initialValue = 0.0f,
        targetValue = if (isDark) 0.06f else 0.0f,
        animationSpec = infiniteRepeatable(animation = tween(4000), repeatMode = RepeatMode.Reverse),
        label = "breath"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

                    // 日期行 + 设置按钮
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
                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreHoriz,
                                contentDescription = "更多",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 问题卡片（4:3 比例）
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 32.dp, vertical = 20.dp)
                            ) {
                                // "今日问题" 居中
                                Text(
                                    "今日问题",
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(Modifier.height(16.dp))

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
                                        QuestionContent(question = currentQuestion)
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "日课一问，破局人生",
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.35f)
                                )
                            }

                            // 分享按钮
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        shareBitmap = withContext(Dispatchers.IO) {
                                            generateShareBitmap(context, currentQuestion)
                                        }
                                        if (shareBitmap != null) {
                                            showShareSheet = true
                                        } else {
                                            snackbarHostState.showSnackbar("图片生成失败，请稍后重试")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.FileDownload,
                                    contentDescription = "下载到相册",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
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
                            DotIndicator(currentIndex = currentIndex, totalCount = totalCount, activeColor = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("换一问", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
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

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }

    // 首次使用引导
    if (showOnboarding) {
        OnboardingSheet(
            onDismiss = {
                showOnboarding = false
                prefs.edit().putBoolean("has_seen_onboarding", true).apply()
            }
        )
    }

    // 设置面板
    if (showSettings) {
        SettingsSheet(
            currentOption = darkModeOption,
            onOptionSelected = { option ->
                darkModeOption = option
                prefs.edit().putString("dark_mode", option.name).apply()
                (context as? android.app.Activity)?.recreate()
            },
            onDismiss = { showSettings = false }
        )
    }

    // 分享弹窗
    if (showShareSheet && shareBitmap != null) {
        AlertDialog(
            onDismissRequest = { showShareSheet = false; shareBitmap = null },
            title = { Text("分享日课一问") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    saveToGallery(context, shareBitmap!!)
                                }
                                snackbarHostState.showSnackbar("卡片已保存到相册")
                            }
                            showShareSheet = false
                            shareBitmap = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("保存到相册")
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    shareImage(context, shareBitmap!!)
                                }
                            }
                            showShareSheet = false
                            shareBitmap = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("分享到...")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showShareSheet = false
                    shareBitmap = null
                }) { Text("取消") }
            }
        )
    }
}
