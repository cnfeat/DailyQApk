package com.dailyquestion

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class DailyQuestionWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val isDark = context.isNightMode()
            val question = DailyQuestionData.getRandomQuestion(context)

            val bgColor = if (isDark)
                ColorProvider(com.dailyquestion.R.color.widget_bg_dark)
            else
                ColorProvider(com.dailyquestion.R.color.widget_bg_light)

            val textColor = if (isDark)
                ColorProvider(com.dailyquestion.R.color.widget_text_dark)
            else
                ColorProvider(com.dailyquestion.R.color.widget_text_light)

            val hintColor = if (isDark)
                ColorProvider(com.dailyquestion.R.color.widget_hint_dark)
            else
                ColorProvider(com.dailyquestion.R.color.widget_hint_light)

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
                    .background(bgColor)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.Start
            ) {
                // 左侧：品牌
                Column(modifier = GlanceModifier.padding(end = 12.dp)) {
                    Text(
                        text = "日课一问",
                        style = TextStyle(color = textColor, fontSize = 13.sp, textAlign = TextAlign.Start),
                        maxLines = 1
                    )
                    Text(
                        text = "破局人生",
                        style = TextStyle(color = hintColor, fontSize = 10.sp, textAlign = TextAlign.Start),
                        maxLines = 1
                    )
                }

                // 右侧：问题内容（占剩余空间）
                Text(
                    text = question,
                    style = TextStyle(color = textColor, fontSize = 11.sp, textAlign = TextAlign.Start),
                    maxLines = 2,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }

    private fun Context.isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}
