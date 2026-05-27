package com.dailyquestion

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.Dp
import androidx.glance.unit.Sp

/**
 * 日课一问 — Glance Widget
 *
 * 桌面小部件，显示一条日课问题。
 * 点击打开 App 查看完整追问或换题。
 * 每日 8:00 由 WorkManager 自动刷新题目。
 */
class DailyQuestionWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val isDarkMode = context.isNightMode()
            val question = DailyQuestionData.getRandomQuestion(context)

            val bgColor = if (isDarkMode)
                ColorProvider(com.dailyquestion.R.color.widget_bg_dark)
            else
                ColorProvider(com.dailyquestion.R.color.widget_bg_light)

            val textColor = if (isDarkMode)
                ColorProvider(com.dailyquestion.R.color.widget_text_dark)
            else
                ColorProvider(com.dailyquestion.R.color.widget_text_light)

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(Dp(12f))
                    .background(bgColor)
                    .clickable(
                        actionStartActivity<MainActivity>(
                            intent = { Intent(context, MainActivity::class.java) }
                        )
                    ),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = question,
                    style = TextStyle(
                        color = textColor,
                        fontSize = Sp(14f),
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 4
                )
            }
        }
    }

    private fun Context.isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}
