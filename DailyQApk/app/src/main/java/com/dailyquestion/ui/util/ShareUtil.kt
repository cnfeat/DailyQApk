package com.dailyquestion.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.dailyquestion.model.Question
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * 将问题渲染为分享/保存用的卡片图片（1080×1540）。
 *
 * 三区布局：
 *   1. 顶部「今日问题」居中
 *   2. 中区主问题（最大容量 5 行，固定分隔线位置）
 *   3. 下区扩展文字
 * 分隔线始终在同一位置，问题在所属区域内垂直居中。
 * 底部「日课一问 · 破局人生」
 */
fun generateShareBitmap(context: Context, question: Question): Bitmap? {
    return try {
        val width = 1080
        val height = 1540

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景
        val bgPaint = Paint().apply { color = Color.parseColor("#FFF0FBF4") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 白色卡片（边距加大）
        val margin = 80f
        val cardRadius = 48f
        val cardRect = RectF(margin, margin, width - margin, height - margin)
        val cardPaint = Paint().apply { color = Color.WHITE; isAntiAlias = true }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, cardPaint)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#12000000"); style = Paint.Style.STROKE
            strokeWidth = 1.5f; isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, borderPaint)

        // ==================== 固定坐标系统 ====================
        val textLeft = 130f
        val textMaxWidth = width - textLeft * 2f
        val cardTop = margin
        val cardBottom = height - margin

        // 区域 1：今日问题 — 居中
        val labelY = cardTop + 110f
        val labelPaint = Paint().apply {
            color = Color.parseColor("#FF888888"); textSize = 36f; isAntiAlias = true
        }
        val labelText = "今日问题"
        canvas.drawText(labelText, width / 2f - labelPaint.measureText(labelText) / 2f, labelY, labelPaint)

        // 分隔线固定位置（基于最大容量 5 行）
        val separatorY = cardTop + 600f

        // 区域 2：主问题 — 在标签和分隔线之间居中
        val questionFontSize = 62f
        val questionLineH = 88f
        val qPaint = Paint().apply {
            color = Color.parseColor("#FF07C160"); textSize = questionFontSize; isAntiAlias = true
        }
        val qLines = wrapText(question.question, qPaint, textMaxWidth)

        val questionZoneTop = labelY + 50f
        val questionZoneBottom = separatorY - 40f
        val questionZoneH = questionZoneBottom - questionZoneTop

        val actualQuestionH = (qLines.size * questionLineH).coerceAtMost(questionZoneH)
        val questionStartY = questionZoneTop + (questionZoneH - actualQuestionH) / 2f + questionLineH * 0.7f

        var y = questionStartY
        for (line in qLines) {
            if (y > questionZoneBottom) break
            canvas.drawText(line, textLeft, y, qPaint)
            y += questionLineH
        }

        // 固定分隔线
        val sepPaint = Paint().apply { color = Color.parseColor("#FF07C160"); alpha = 50; strokeWidth = 2f }
        canvas.drawLine(width / 2f - 100f, separatorY, width / 2f + 100f, separatorY, sepPaint)

        // 区域 3：扩展文字（如果有）
        if (question.extension.isNotBlank()) {
            val extStartY = separatorY + 60f
            val extFontSize = 40f
            val extLineH = 50f
            val extPaint = Paint().apply {
                color = Color.parseColor("#FF888888"); textSize = extFontSize; isAntiAlias = true
            }
            val extLines = wrapText(question.extension, extPaint, textMaxWidth - 40f)
            var ey = extStartY
            val extMaxBottom = cardBottom - 120f
            for (line in extLines) {
                if (ey + extLineH > extMaxBottom) break
                canvas.drawText(line, textLeft + 20f, ey, extPaint)
                ey += extLineH
            }
        }

        // 底部水印
        val footerPaint = Paint().apply {
            color = Color.parseColor("#FF07C160"); textSize = 30f; isAntiAlias = true; alpha = 60
        }
        canvas.drawText("日课一问 · 破局人生", width / 2f - footerPaint.measureText("日课一问 · 破局人生") / 2f, cardBottom - 50f, footerPaint)

        bitmap
    } catch (_: Exception) { null }
}

fun shareImage(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "share_images")
        cachePath.mkdirs()
        val file = File(cachePath, "image.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                putExtra(Intent.EXTRA_STREAM, contentUri)
            }
            context.startActivity(Intent.createChooser(shareIntent, "分享日课"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun saveToGallery(context: Context, bitmap: Bitmap) {
    val filename = "DailyQuestion_${System.currentTimeMillis()}.jpg"
    var fos: OutputStream? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.contentResolver?.also { resolver ->
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }
    } else {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
    }
    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show()
    }
}

private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
    val lines = mutableListOf<String>()
    var start = 0
    while (start < text.length) {
        val count = paint.breakText(text, start, text.length, true, maxWidth, null)
        if (count <= 0) break
        lines.add(text.substring(start, start + count))
        start += count
    }
    return lines
}
