package com.dailyquestion

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dailyquestion.model.Question
import com.dailyquestion.model.QuestionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * 日课一问 — 主 Activity
 *
 * 卡片式主界面，展示今日问题。
 * 核心交互：
 * - 打开即显示今日问题
 * - 点击「换一问」切换问题（每日最多 3 次）
 * - 点击卡片展开/收起深度追问
 */
class MainActivity : AppCompatActivity() {

    private lateinit var questionManager: QuestionManager

    private lateinit var tvDate: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvExtensionLabel: TextView
    private lateinit var tvExtension: TextView
    private lateinit var cardQuestion: View
    private lateinit var btnSwitch: TextView
    private lateinit var tvRemaining: TextView

    private var isExtensionVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        questionManager = QuestionManager.getInstance(this)

        initViews()
        setupListeners()
        loadTodayQuestion()

        // 调度 Widget 每日定时刷新（首次启动时注册一次即可）
        DailyQuestionWorker.scheduleDailyUpdate(this)
    }

    private fun initViews() {
        tvDate = findViewById(R.id.tv_date)
        tvQuestion = findViewById(R.id.tv_question)
        tvExtensionLabel = findViewById(R.id.tv_extension_label)
        tvExtension = findViewById(R.id.tv_extension)
        cardQuestion = findViewById(R.id.card_question)
        btnSwitch = findViewById(R.id.btn_switch)
        tvRemaining = findViewById(R.id.tv_remaining)

        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE)
        tvDate.text = "${today.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))} $dayOfWeek"
    }

    private fun setupListeners() {
        cardQuestion.setOnClickListener {
            toggleExtension()
        }

        btnSwitch.setOnClickListener {
            handleSwitchQuestion()
        }
    }

    private fun loadTodayQuestion() {
        val question = questionManager.getTodayQuestion()
        displayQuestion(question)
        updateSwitchButton()
    }

    private fun displayQuestion(question: Question) {
        tvQuestion.text = question.question
        tvExtension.text = question.extension

        if (question.extension.isNotBlank()) {
            tvExtensionLabel.visibility = View.VISIBLE
        } else {
            tvExtensionLabel.visibility = View.GONE
        }

        isExtensionVisible = false
        tvExtension.visibility = View.GONE
    }

    private fun toggleExtension() {
        isExtensionVisible = !isExtensionVisible
        tvExtension.visibility = if (isExtensionVisible) View.VISIBLE else View.GONE
        tvExtensionLabel.text = if (isExtensionVisible) "▲ 收起追问" else "💭 深度追问"
    }

    private fun handleSwitchQuestion() {
        val nextQuestion = questionManager.switchToNext()
        if (nextQuestion != null) {
            displayQuestion(nextQuestion)
            updateSwitchButton()
            // 同步刷新桌面 Widget
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    WidgetUpdater.refreshAll(this@MainActivity)
                }
            }
        }
    }

    private fun updateSwitchButton() {
        val remaining = questionManager.getRemainingSwitches()

        if (remaining <= 0) {
            btnSwitch.isEnabled = false
            btnSwitch.background = ContextCompat.getDrawable(this, R.drawable.button_disabled)
            btnSwitch.text = "今日已用完"
            tvRemaining.text = "今日换问次数已用完，明天再来"
        } else {
            btnSwitch.isEnabled = true
            btnSwitch.background = ContextCompat.getDrawable(this, R.drawable.button_primary)
            btnSwitch.text = "换一问（剩${remaining}次）"
            tvRemaining.text = "每日可换 $remaining 次"
        }
    }
}
