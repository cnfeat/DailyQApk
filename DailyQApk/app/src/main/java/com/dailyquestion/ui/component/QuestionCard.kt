package com.dailyquestion.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyquestion.model.Question

/**
 * 问题内容：主问题 + 拓展文字（无标记）
 */
@Composable
fun QuestionContent(
    question: Question,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            question.question,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 22.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start
        )

        if (question.extension.isNotBlank()) {
            Spacer(Modifier.height(20.dp))

            Box(
                Modifier
                    .width(28.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(0.2f))
            )

            Spacer(Modifier.height(14.dp))

            Text(
                question.extension,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                textAlign = TextAlign.Start
            )
        }
    }
}
