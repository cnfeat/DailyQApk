package com.dailyquestion

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAppWidgetState
import com.dailyquestion.model.QuestionManager

/**
 * 日课一问 — Widget 更新工具类
 *
 * 在 App 内操作（如切换问题）后，同步刷新 Widget 显示。
 */
object WidgetUpdater {

    /**
     * 通知所有 Widget 实例重新渲染。
     * 调用 provideGlance → getRandomQuestion() → QuestionManager.getTodayQuestion()
     */
    suspend fun refreshAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(DailyQuestionWidget::class.java)
        if (glanceIds.isEmpty()) return

        glanceIds.forEach { glanceId ->
            DailyQuestionWidget().update(context, glanceId)
        }
    }
}
