package com.dailyquestion

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * 5×2 标准 Widget Receiver（仅主问题）
 */
class DailyQuestionWidgetReceiver5x2 : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyQuestionWidget()
}
