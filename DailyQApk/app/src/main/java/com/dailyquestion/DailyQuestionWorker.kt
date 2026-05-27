package com.dailyquestion

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * 日课一问 — WorkManager Worker
 *
 * 每天早 8:00 刷新 Widget 显示。
 * Widget 的无状态刷新触发 provideGlance → getQuestion()
 * → QuestionManager.getTodayQuestion()，日期变更时自动换题。
 */
class DailyQuestionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val PERIODIC_WORK_NAME = "daily_question_periodic_update"
        private const val TARGET_HOUR = 8
        private const val TARGET_MINUTE = 0

        fun scheduleDailyUpdate(context: Context) {
            val now = LocalDateTime.now()
            val today8am = now.toLocalDate().atTime(TARGET_HOUR, TARGET_MINUTE)
            val nextRun = if (now.isBefore(today8am) || now.toLocalTime().equals(LocalTime.of(TARGET_HOUR, TARGET_MINUTE))) {
                today8am
            } else {
                today8am.plusDays(1)
            }
            val initialDelayMillis = Duration.between(now, nextRun).toMillis()

            val request = PeriodicWorkRequestBuilder<DailyQuestionWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .addTag(PERIODIC_WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            WidgetUpdater.refreshAll(context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
