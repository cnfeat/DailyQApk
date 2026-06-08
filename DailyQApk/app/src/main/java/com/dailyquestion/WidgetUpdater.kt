package com.dailyquestion

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState

/**
 * 日课一问 — Widget 更新工具类
 */
object WidgetUpdater {

    suspend fun refreshAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val q = DailyQuestionData.getCurrentQuestion(context)
        val ids = manager.getGlanceIds(DailyQuestionWidgetLarge::class.java)
        ids.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[DailyQuestionWidgetLarge.KEY_QUESTION] = q.question
                prefs[DailyQuestionWidgetLarge.KEY_EXTENSION] = q.extension
            }
            DailyQuestionWidgetLarge().update(context, glanceId)
        }
    }
}
