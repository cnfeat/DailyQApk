package com.dailyquestion.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyquestion.model.Question
import com.dailyquestion.ui.util.generateShareBitmap
import com.dailyquestion.ui.util.saveToGallery
import com.dailyquestion.ui.util.shareImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
    question: Question,
    onDismiss: () -> Unit,
    scope: CoroutineScope
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("分享今日日课",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(20.dp))

            // 分享到其他 App
            ShareOptionRow(
                icon = "📤",
                title = "分享到其他 App",
                subtitle = "微信、朋友圈、微博等",
                onClick = {
                    scope.launch {
                        onDismiss()
                        val bitmap = generateShareBitmap(context, question)
                        if (bitmap != null) shareImage(context, bitmap)
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // 保存到本地相册
            ShareOptionRow(
                icon = "💾",
                title = "保存到本地相册",
                subtitle = "保存为高清卡片图片",
                onClick = {
                    scope.launch {
                        onDismiss()
                        val bitmap = generateShareBitmap(context, question)
                        if (bitmap != null) saveToGallery(context, bitmap)
                    }
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ShareOptionRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
