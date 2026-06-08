package com.dailyquestion

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
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
        val q = DailyQuestionData.getCurrentQuestion(context)

        // 更新标准 Widget
        val smallIds = manager.getGlanceIds(DailyQuestionWidget::class.java)
        smallIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[DailyQuestionWidget.KEY_QUESTION] = q.question
            }
            DailyQuestionWidget().update(context, glanceId)
        }

        // 更新大号 Widget（含扩展文字）
        val largeIds = manager.getGlanceIds(DailyQuestionWidgetLarge::class.java)
        largeIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[DailyQuestionWidgetLarge.KEY_QUESTION] = q.question
                prefs[DailyQuestionWidgetLarge.KEY_EXTENSION] = q.extension
            }
            DailyQuestionWidgetLarge().update(context, glanceId)
        }
    }
}
