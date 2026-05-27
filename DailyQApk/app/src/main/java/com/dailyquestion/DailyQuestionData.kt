package com.dailyquestion

import android.content.Context
import com.dailyquestion.model.QuestionManager

/**
 * 日课一问 — Widget 数据桥接层
 *
 * 桥接 QuestionManager 与无状态 Widget 的数据需求。
 * Widget 渲染时调用此方法获取今日问题。
 */
object DailyQuestionData {

    /**
     * 获取今日问题文字。
     * 通过 QuestionManager 获取，日期变更时自动选新题。
     */
    fun getRandomQuestion(context: Context): String {
        val manager = QuestionManager.getInstance(context)
        return manager.getTodayQuestion().question
    }
}
