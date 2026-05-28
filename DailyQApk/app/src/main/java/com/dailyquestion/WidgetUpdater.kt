package com.dailyquestion

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState

/**
 * 日课一问 — Widget 更新工具类
 *
 * 在 App 内换题后同步刷新桌面 Widget。
 */
object WidgetUpdater {

    /**
     * 获取今日最新问题并更新所有 Widget 实例。
     */
    suspend fun refreshAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(DailyQuestionWidget::class.java)
        if (glanceIds.isEmpty()) return

        val newQuestion = DailyQuestionData.getRandomQuestion(context)

        glanceIds.forEach { glanceId ->
            // 先更新 Glance 状态
            updateAppWidgetState(
                context = context,
                glanceId = glanceId
            ) { prefs ->
                prefs[DailyQuestionWidget.KEY_QUESTION] = newQuestion
            }
            // 然后触发重绘
            DailyQuestionWidget().update(context, glanceId)
        }
    }
}
