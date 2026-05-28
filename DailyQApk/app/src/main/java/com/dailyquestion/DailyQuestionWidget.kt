package com.dailyquestion

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * 日课一问 — Widget
 *
 * 顶部：日课一问 · 破局人生（横排）
 * 中间：问题文字
 * 底部：「换一问」点击切换
 * 整体点击打开 App。
 */
class DailyQuestionWidget : GlanceAppWidget() {

    companion object {
        val KEY_QUESTION = stringPreferencesKey("current_question")
    }

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            var question = prefs[KEY_QUESTION]
            if (question == null) {
                question = DailyQuestionData.getRandomQuestion(context)
            }

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

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .background(bgColor)
            ) {
                // === 顶部：标题行 ===
                Text(
                    text = "日课一问 · 破局人生",
                    style = TextStyle(
                        color = hintColor,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier
                        .padding(bottom = 6.dp)
                        .clickable(actionStartActivity<MainActivity>())
                )

                // === 中间：问题文字 ===
                Text(
                    text = question,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 15.sp
                    ),
                    maxLines = 4,
                    modifier = GlanceModifier
                        .defaultWeight()
                        .padding(bottom = 4.dp)
                        .clickable(actionStartActivity<MainActivity>())
                )

                // === 底部：换一问 ===
                Text(
                    text = "换一问",
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionRunCallback<SwitchQuestionAction>()),
                    style = TextStyle(
                        color = hintColor,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End
                    ),
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

/**
 * 换一问 ActionCallback
 */
class SwitchQuestionAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val nextQuestion = DailyQuestionData.getNextQuestion(context)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[DailyQuestionWidget.KEY_QUESTION] = nextQuestion
        }
        DailyQuestionWidget().update(context, glanceId)
    }
}
