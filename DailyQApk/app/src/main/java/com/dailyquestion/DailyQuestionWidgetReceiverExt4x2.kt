package com.dailyquestion

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * 4×2 扩展 Widget Receiver（主问题+扩展文字）
 */
class DailyQuestionWidgetReceiverExt4x2 : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyQuestionWidgetLarge()
}
