package com.dailyquestion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.lifecycleScope
import com.dailyquestion.model.QuestionManager
import com.dailyquestion.ui.screen.DarkModeOption
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
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    WidgetUpdater.refreshAll(this@MainActivity)
                }
            }
        }

        setContent {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val darkModeStr = prefs.getString("dark_mode", "SYSTEM") ?: "SYSTEM"
            val isDark = when (DarkModeOption.valueOf(darkModeStr)) {
                DarkModeOption.SYSTEM -> isSystemInDarkTheme()
                DarkModeOption.DARK -> true
                DarkModeOption.LIGHT -> false
            }

            DailyQuestionTheme(darkTheme = isDark) {
                MainScreen(questionManager = questionManager, context = this@MainActivity)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getStringExtra("action") == "switch_question") {
            questionManager.switchToNext()
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    WidgetUpdater.refreshAll(this@MainActivity)
                }
            }
            recreate()
        }
    }
}
