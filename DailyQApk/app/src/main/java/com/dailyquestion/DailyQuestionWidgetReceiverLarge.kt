package com.dailyquestion

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * 日课一问 — 5×2 大尺寸 Widget Receiver
 */
class DailyQuestionWidgetReceiverLarge : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = DailyQuestionWidget()
}
