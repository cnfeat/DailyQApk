package com.dailyquestion

import android.content.Context
import com.dailyquestion.model.Question
import com.dailyquestion.model.QuestionManager

/**
 * 日课一问 — Widget 数据桥接层
 *
 * 桥接 QuestionManager 与 Widget/Worker 的数据需求。
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

    /**
     * 获取下一个问题文字（换一问）。
     * 循环切换：1→2→3→1
     */
    fun getNextQuestion(context: Context): String {
        val manager = QuestionManager.getInstance(context)
        return manager.switchToNext().question
    }

    /**
     * 获取今日当前问题对象（含扩展文字）。
     */
    fun getCurrentQuestion(context: Context): Question {
        val manager = QuestionManager.getInstance(context)
        return manager.getTodayQuestion()
    }
}
