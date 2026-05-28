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
 * 每日随机抽取 3 道题，循环浏览 (1/3 → 2/3 → 3/3 → 1/3 ...)。
 * 日期变更时自动重置题库。
 *
 * 使用方式（单例）：
 *   val manager = QuestionManager.getInstance(context)
 *   val question = manager.getTodayQuestion()
 *   val next = manager.switchToNext()
 *   val (index, total) = manager.getCurrentProgress()  // (0, 3)
 */
class QuestionManager private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "daily_question_prefs"
        private const val KEY_TODAY_DATE = "today_date"
        private const val KEY_TODAY_QUESTION_ID = "today_question_id"
        private const val KEY_TODAY_QUESTION_IDS = "today_question_ids"
        private const val KEY_CURRENT_INDEX = "current_index"
        private const val DAILY_COUNT = 3

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

    /** 完整问题库（惰性加载） */
    private var questionList: List<Question>? = null

    private fun loadQuestions(): List<Question> {
        if (questionList != null) return questionList!!

        return try {
            val json = context.applicationContext.resources
                .openRawResource(com.dailyquestion.R.raw.questions)
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<Question>>() {}.type
            val list: List<Question> = gson.fromJson(json, type)
            val safeList = list ?: emptyList()
            questionList = safeList
            safeList
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * 获取今日当前问题。
     * 新的一天自动初始化 3 道题，索引归零。
     */
    fun getTodayQuestion(): Question {
        val today = todayStr()
        val savedDate = prefs.getString(KEY_TODAY_DATE, null)

        if (savedDate == today) {
            // 同一天 → 读取已分配的问题
            val ids = getDailyQuestionIds()
            val index = prefs.getInt(KEY_CURRENT_INDEX, 0)
            if (ids.isNotEmpty() && index < ids.size) {
                val questions = loadQuestions()
                val found = questions.find { it.id == ids[index] }
                if (found != null) return found
            }
        }

        // 新的一天 → 随机选 3 题，索引归零
        return initDailyQuestions()
    }

    /**
     * 切换至下一题（循环：1→2→3→1）。
     */
    fun switchToNext(): Question {
        val ids = getDailyQuestionIds()
        val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        val nextIndex = (currentIndex + 1) % ids.size

        prefs.edit()
            .putInt(KEY_CURRENT_INDEX, nextIndex)
            .apply()

        val questions = loadQuestions()
        return questions.find { it.id == ids[nextIndex] }
            ?: questions.first()
    }

    /**
     * 获取当前进度位置。
     * @return Pair(currentIndex: Int, totalCount: Int) 例如 (0, 3) 表示第 1/3
     */
    fun getCurrentProgress(): Pair<Int, Int> {
        val index = prefs.getInt(KEY_CURRENT_INDEX, 0)
        return Pair(index, DAILY_COUNT)
    }

    /**
     * 获取当前问题索引（用于 Widget 等场景）。
     */
    fun getTodayQuestionIndex(): Int {
        return prefs.getInt(KEY_CURRENT_INDEX, 0)
    }

    // ==================== 内部方法 ====================

    private fun initDailyQuestions(): Question {
        val questions = loadQuestions().toMutableList()
        if (questions.isEmpty()) {
            return Question(id = "0", question = "今天也要好好思考", extension = "")
        }

        val picked = mutableListOf<Question>()
        val pool = questions.toMutableList()

        repeat(DAILY_COUNT.coerceAtMost(pool.size)) {
            val q = pool.random()
            picked.add(q)
            pool.remove(q)
        }

        val ids = picked.map { it.id }
        val today = todayStr()

        prefs.edit()
            .putString(KEY_TODAY_DATE, today)
            .putString(KEY_TODAY_QUESTION_IDS, gson.toJson(ids))
            .putInt(KEY_CURRENT_INDEX, 0)
            .apply()

        return picked.first()
    }

    private fun getDailyQuestionIds(): List<String> {
        val json = prefs.getString(KEY_TODAY_QUESTION_IDS, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return try { gson.fromJson(json, type) } catch (_: Exception) { emptyList() }
    }

    private fun todayStr(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
