package com.dailyquestion

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

/**
 * 日课一问 — Widget 更新工具类
 */
object WidgetUpdater {

    suspend fun refreshAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(DailyQuestionWidget::class.java)
        glanceIds.forEach { glanceId ->
            DailyQuestionWidget().update(context, glanceId)
        }
    }
}
