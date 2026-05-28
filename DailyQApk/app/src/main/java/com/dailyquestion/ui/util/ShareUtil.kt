package com.dailyquestion.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.dailyquestion.model.Question
import java.io.File
import java.io.FileOutputStream

/**
 * 将问题渲染为分享/保存用的卡片图片（1080×1440，浅绿背景）。
 */
fun generateShareBitmap(context: Context, question: Question): Bitmap? {
    return try {
        val width = 1080
        val height = 1540

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景色
        canvas.drawColor(android.graphics.Color.parseColor("#FFF1F8E9"))

        // 上装饰条
        val decorPaint = Paint().apply { color = android.graphics.Color.parseColor("#FF2E7D32"); alpha = 40 }
        canvas.drawRect(0f, 0f, width.toFloat(), 8f, decorPaint)

        // 🌱 + 标题
        val iconPaint = Paint().apply { textSize = 56f; isAntiAlias = true }
        canvas.drawText("🌱", 60f, 120f, iconPaint)
        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FF49454F"); textSize = 28f
            isAntiAlias = true; alpha = 120
        }
        canvas.drawText("日课一问", 60f, 160f, titlePaint)

        // 分隔线
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FF2E7D32"); alpha = 50; strokeWidth = 2f
        }
        canvas.drawLine(60f, 200f, width - 60f, 200f, linePaint)

        // 主问题
        val questionPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FF1B1B1F"); textSize = 42f
            isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD
        }
        var y = 280f
        val lineHeight = 60f
        val maxWidth = width - 120f
        for (line in wrapText(question.question, questionPaint, maxWidth)) {
            canvas.drawText(line, 60f, y, questionPaint); y += lineHeight
        }

        // 拓展文字
        if (question.extension.isNotBlank()) {
            y += 20f
            val extPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#FF666666"); textSize = 30f
                isAntiAlias = true
            }
            for (line in wrapText(question.extension, extPaint, maxWidth)) {
                canvas.drawText(line, 80f, y, extPaint); y += 36f
            }
        }

        // 底部水印
        y += 60f
        val footerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FF2E7D32"); textSize = 24f
            isAntiAlias = true; alpha = 80
        }
        canvas.drawText("—— 日课一问 · 每天一个好问题，破局人生", 60f, y, footerPaint)

        // 下装饰条
        canvas.drawRect(0f, (height - 8).toFloat(), width.toFloat(), height.toFloat(), decorPaint)

        bitmap
    } catch (_: Exception) { null }
}

/**
 * 分享图片到其他 App。
 */
fun shareImage(context: Context, bitmap: Bitmap) {
    try {
        val file = saveToCache(context, bitmap)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享今日日课"))
    } catch (_: Exception) { }
}

/**
 * 保存图片到系统相册。
 * Android 10+ 使用 MediaStore，旧版本写入公共目录。
 */
fun saveToGallery(context: Context, bitmap: Bitmap) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+：MediaStore
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "日课一问_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/日课一问")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
        } else {
            // 旧版本：写入公共 Pictures 目录
            val dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + "/日课一问"
            )
            dir.mkdirs()
            val file = File(dir, "日课一问_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            // 通知相册刷新
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.fromFile(file)
            context.sendBroadcast(intent)
        }
    } catch (_: Exception) { }
}

/** 保存 bitmap 到缓存目录 */
private fun saveToCache(context: Context, bitmap: Bitmap): File {
    val cacheDir = File(context.cacheDir, "share_images")
    cacheDir.mkdirs()
    val file = File(cacheDir, "daily_question_share.png")
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    return file
}

/** 文本自动换行 */
private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
    val lines = mutableListOf<String>()
    val sb = StringBuilder()
    for (char in text.toCharArray()) {
        if (char == '\n') { lines.add(sb.toString()); sb.clear(); continue }
        sb.append(char)
        if (paint.measureText(sb.toString()) > maxWidth) {
            sb.deleteCharAt(sb.length - 1)
            lines.add(sb.toString()); sb.clear(); sb.append(char)
        }
    }
    if (sb.isNotEmpty()) lines.add(sb.toString())
    return lines
}
