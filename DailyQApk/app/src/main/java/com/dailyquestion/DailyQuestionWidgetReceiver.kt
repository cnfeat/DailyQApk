package com.dailyquestion

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * 日课一问 — Glance Widget Receiver
 *
 * Glance 框架要求的 Widget Receiver 声明类。
 * 在 AndroidManifest.xml 中注册此 Receiver。
 */
class DailyQuestionWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = DailyQuestionWidget()
}
