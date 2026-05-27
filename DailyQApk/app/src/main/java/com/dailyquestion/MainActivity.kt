package com.dailyquestion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.dailyquestion.model.QuestionManager
import com.dailyquestion.ui.screen.MainScreen
import com.dailyquestion.ui.theme.DailyQuestionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var questionManager: QuestionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        questionManager = QuestionManager.getInstance(this)

        // 检测是否来自 Widget 的切换请求
        if (intent?.getStringExtra("action") == "switch_question") {
            questionManager.switchToNext()
            // 刷新 Widget
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    WidgetUpdater.refreshAll(this@MainActivity)
                }
            }
        }

        setContent {
            DailyQuestionTheme {
                MainScreen(questionManager = questionManager, context = this)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 当 Activity 已存在时处理 Widget 切换
        if (intent.getStringExtra("action") == "switch_question") {
            questionManager.switchToNext()
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    WidgetUpdater.refreshAll(this@MainActivity)
                }
            }
            // 重建界面
            recreate()
        }
    }
}
