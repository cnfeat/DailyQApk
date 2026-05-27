package com.dailyquestion.model

/**
 * 日课一问 — 问题数据模型
 *
 * 对应 data.json / questions.json 中每一条记录的结构。
 * id 按"001""002"递增，question 为主问题，extension 为拓展思考引导。
 */
data class Question(
    val id: String,
    val question: String,
    val extension: String
)
