package com.dailyquestion.model

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 日课一问 — 每日问题状态管理器
 *
 * 职责：
 * 1. 从 SharedPreferences 读取/写入每日状态
 * 2. 从 res/raw/questions.json 加载完整问题库
 * 3. 调度每日问题（日期变更时自动重置）
 * 4. 管理「换一问」限次逻辑（每日最多 3 次）
 *
 * 使用方式（单例）：
 *   val manager = QuestionManager.getInstance(context)
 *   val question = manager.getTodayQuestion()
 *   val next = manager.switchToNext()
 *   val remaining = manager.getRemainingSwitches()
 */
class QuestionManager private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "daily_question_prefs"
        private const val KEY_TODAY_DATE = "today_date"
        private const val KEY_TODAY_QUESTION_ID = "today_question_id"
        private const val KEY_SWITCH_COUNT = "switch_count"
        private const val MAX_SWITCHES = 3

        @Volatile
        private var instance: QuestionManager? = null

        fun getInstance(context: Context): QuestionManager {
            return instance ?: synchronized(this) {
                instance ?: QuestionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    /** 完整问题库（惰性加载，仅首次访问时解析 JSON） */
    private var questionList: List<Question>? = null

    /**
     * 从 res/raw/questions.json 加载问题库。
     * 使用 Gson 解析为 List<Question>，结果内部缓存。
     */
    private fun loadQuestions(): List<Question> {
        if (questionList != null) return questionList!!

        val json = context.applicationContext.resources
            .openRawResource(com.dailyquestion.R.raw.questions)
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<Question>>() {}.type
        val list: List<Question> = gson.fromJson(json, type)
        questionList = list
        return list
    }

    /**
     * 获取或初始化今日问题。
     *
     * 逻辑：
     * - 如果本地存储的日期与今天相同 → 直接返回已分配的问题
     * - 如果日期不同（新的一天）→ 随机选新问题，重置 switchCount=0
     */
    fun getTodayQuestion(): Question {
        val today = todayStr()
        val savedDate = prefs.getString(KEY_TODAY_DATE, null)

        if (savedDate == today) {
            // 同一天 → 读取已分配的问题
            val savedId = prefs.getString(KEY_TODAY_QUESTION_ID, null)
            if (savedId != null) {
                val questions = loadQuestions()
                return questions.find { it.id == savedId } ?: pickRandomQuestion()
            }
        }

        // 新的一天 → 重置
        return pickRandomQuestion().also {
            prefs.edit()
                .putString(KEY_TODAY_DATE, today)
                .putString(KEY_TODAY_QUESTION_ID, it.id)
                .putInt(KEY_SWITCH_COUNT, 0)
                .apply()
        }
    }

    /**
     * 执行「换一问」操作。
     *
     * @return Question? 返回新问题，如果今日已用完 3 次则返回 null
     */
    fun switchToNext(): Question? {
        val count = prefs.getInt(KEY_SWITCH_COUNT, 0)
        if (count >= MAX_SWITCHES) return null

        val questions = loadQuestions()
        val currentId = prefs.getString(KEY_TODAY_QUESTION_ID, null)

        // 随机选一个与当前不同的题目
        val candidates = questions.filter { it.id != currentId }
        val next = candidates.random()

        prefs.edit()
            .putString(KEY_TODAY_QUESTION_ID, next.id)
            .putString(KEY_TODAY_DATE, todayStr())
            .putInt(KEY_SWITCH_COUNT, count + 1)
            .apply()

        return next
    }

    /**
     * 返回今日剩余可切换次数。
     */
    fun getRemainingSwitches(): Int {
        val count = prefs.getInt(KEY_SWITCH_COUNT, 0)
        return (MAX_SWITCHES - count).coerceAtLeast(0)
    }

    /** 返回今日最大可切换次数（常量值） */
    fun getMaxSwitches(): Int = MAX_SWITCHES

    /**
     * 获取当前问题在问题库中的索引（用于 Widget 等场景）。
     */
    fun getTodayQuestionIndex(): Int {
        val questions = loadQuestions()
        val savedId = prefs.getString(KEY_TODAY_QUESTION_ID, null)
        return questions.indexOfFirst { it.id == savedId }.coerceAtLeast(0)
    }

    private fun pickRandomQuestion(): Question {
        val questions = loadQuestions()
        return questions.random()
    }

    private fun todayStr(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
