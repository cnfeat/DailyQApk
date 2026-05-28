package com.dailyquestion

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class DailyQuestionWidget : GlanceAppWidget() {

    companion object {
        val KEY_QUESTION = stringPreferencesKey("current_question")
    }

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val question = prefs[KEY_QUESTION] ?: DailyQuestionData.getRandomQuestion(context)

            val isDark = context.isNightMode()
            val bgColor = if (isDark)
                ColorProvider(Color(context.getColor(R.color.widget_bg_dark)))
            else
                ColorProvider(Color(context.getColor(R.color.widget_bg_light)))
            val textColor = if (isDark)
                ColorProvider(Color(context.getColor(R.color.widget_text_dark)))
            else
                ColorProvider(Color(context.getColor(R.color.widget_text_light)))
            val hintColor = if (isDark)
                ColorProvider(Color(context.getColor(R.color.widget_hint_dark)))
            else
                ColorProvider(Color(context.getColor(R.color.widget_hint_light)))
            val lineColor = if (isDark)
                ColorProvider(Color(context.getColor(R.color.widget_line_dark)))
            else
                ColorProvider(Color(context.getColor(R.color.widget_line_light)))

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(bgColor)
            ) {
                // 标题
                Row(
                    modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text("日课一问", style = TextStyle(color = hintColor, fontSize = 14.sp), maxLines = 1)
                    Text(" · ", style = TextStyle(color = hintColor, fontSize = 14.sp), maxLines = 1)
                    Text("破局人生", style = TextStyle(color = hintColor, fontSize = 14.sp), maxLines = 1)
                }

                Box(GlanceModifier.height(6.dp)) {}

                // 分隔线
                Box(GlanceModifier.fillMaxWidth().height(1.dp).background(lineColor)) {}

                // 问题文字
                Text(
                    text = question,
                    style = TextStyle(color = textColor, fontSize = 16.sp, textAlign = TextAlign.Start),
                    maxLines = 4,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .padding(vertical = 8.dp)
                        .clickable(actionStartActivity<MainActivity>())
                )

                // 换一问
                Text(
                    text = "换一问",
                    modifier = GlanceModifier.fillMaxWidth().clickable(actionRunCallback<SwitchQuestionAction>()),
                    style = TextStyle(color = hintColor, fontSize = 13.sp, textAlign = TextAlign.End),
                    maxLines = 1
                )
            }
        }
    }

    private fun Context.isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}

class SwitchQuestionAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        val nextQuestion = DailyQuestionData.getNextQuestion(context)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[DailyQuestionWidget.KEY_QUESTION] = nextQuestion
        }
        DailyQuestionWidget().update(context, glanceId)
    }
}
