package com.dailyquestion.ui.screen

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyquestion.DailyQuestionWorker
import com.dailyquestion.WidgetUpdater
import com.dailyquestion.model.QuestionManager
import com.dailyquestion.ui.theme.*
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
    context: Context = LocalContext.current
) {
    var currentQuestion by remember { mutableStateOf(questionManager.getTodayQuestion()) }
    var currentIndex by remember { mutableStateOf(questionManager.getCurrentProgress().first) }
    val totalCount = 3
    var showSettingsSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { DailyQuestionWorker.scheduleDailyUpdate(context) }

    val today = LocalDate.now()
    val dateStr = "${today.monthValue}月${today.dayOfMonth}日 ${today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE)}"
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { showSettingsSheet = true }) {
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
        ) {
            // 一屏布局：用 weight 分配空间
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // ===== 顶部（固定） =====
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentHeight()
                ) {
                    Text("🌱", fontSize = 26.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("日课一问",
                        style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 4.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f))
                    Text(dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.35f))
                }

                Spacer(Modifier.height(12.dp))

                // ===== 问题卡片（弹性占满剩余空间） =====
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 28.dp)
                    ) {
                        // 顶部呼吸空间（约15%）
                        Spacer(Modifier.weight(0.15f))
                        // 内容区域（约85%）
                        Column(Modifier.weight(0.85f).verticalScroll(rememberScrollState())) {
                            Text(currentQuestion.question,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start)

                            if (currentQuestion.extension.isNotBlank()) {
                                Spacer(Modifier.height(16.dp))
                                Box(Modifier.width(28.dp).height(2.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(0.3f)))
                                Spacer(Modifier.height(12.dp))
                                Text(currentQuestion.extension,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ===== 按钮（固定） =====
                Button(
                    onClick = {
                        val next = questionManager.switchToNext()
                        currentQuestion = next
                        currentIndex = questionManager.getCurrentProgress().first
                        scope.launch { withContext(Dispatchers.IO) { WidgetUpdater.refreshAll(context) } }
                        vibrate(context)
                    },
                    modifier = Modifier.height(44.dp).fillMaxWidth(0.6f),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("换一问 · ${currentIndex + 1}/$totalCount",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                }

                Spacer(Modifier.height(16.dp))

                // ===== 进度模块（固定） =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    YearProgressGrid(modifier = Modifier.padding(14.dp), isDarkMode = isDark)
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Text("日课一问", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("v1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f))
                Spacer(Modifier.height(12.dp))
                Text("效法《论语》「吾日三省吾身」",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun vibrate(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    } catch (_: Exception) { }
}
