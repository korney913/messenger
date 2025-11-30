package com.example.messenger

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

@Composable
fun AvatarImage(context: Context, filePath: String?, modifier: Modifier = Modifier.size(200.dp)) {
    var a = false
    if(filePath!=null){
        val file = File(context.filesDir, filePath)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Avatar",
                modifier = modifier
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {a = true}
    } else {a = true}
    if(a) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Color.Gray)
        )
    }
}

@Composable
fun Photo(
    context: Context, filePath: String?,
    modifier: Modifier = Modifier,
    boxWidth: Dp = 0.dp,
    boxHeight: Dp = 0.dp,
    messageText: String = "",
    overlay: @Composable () -> Unit = {}
) {
    if(filePath!=null){
        val file = File(context.filesDir, filePath)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap != null) {
            if (messageText=="") {
                Box(
                    modifier = modifier
                        .clip(RoundedCornerShape(16.dp))
                        .heightIn(max = boxHeight)
                        .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                        .clipToBounds()
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    overlay()
                }
            }
            else{
                val rolio = bitmap.width.toFloat()/bitmap.height.toFloat()
                val boxHeight = if (boxHeight<=boxWidth/rolio) boxHeight else boxWidth/rolio
                Box(
                    modifier = modifier
                        .clip(RoundedCornerShape(16.dp))
                        .width(boxWidth)
                        .height(boxHeight)
                        .clipToBounds()
                ) {
                    if (rolio < boxWidth/boxHeight)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(20.dp),
                            contentScale = ContentScale.Crop
                        )
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    overlay()
                }
            }
        }
    }
}

fun saveImageToInternalStorage(context: Context, imageUri: Uri, fileName: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        var originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (originalBitmap == null) return null

        val maxDimension = 1024
        if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
            val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
            var newWidth = maxDimension
            var newHeight = maxDimension
            if (ratio > 1) {
                newHeight = (maxDimension / ratio).toInt()
            } else {
                newWidth = (maxDimension * ratio).toInt()
            }
            originalBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        }

        val file = File(context.filesDir, fileName)
        var quality = 100
        var stream: FileOutputStream

        val targetSizeBytes = 75000

        do {
            stream = FileOutputStream(file)
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.flush()
            stream.close()

            val fileSize = file.length()

            if (fileSize > targetSizeBytes) {
                quality -= 10
                if (quality < 10) {
                    val scaled = Bitmap.createScaledBitmap(
                        originalBitmap,
                        (originalBitmap.width * 0.8).toInt(),
                        (originalBitmap.height * 0.8).toInt(),
                        true
                    )
                    if (scaled != originalBitmap) {
                        originalBitmap = scaled
                        quality = 80
                    } else {
                        break
                    }
                }
            }
        } while (file.length() > targetSizeBytes && quality > 0)

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun deleteImageFromInternalStorage(context: Context, fileName: String){
    val file = File(context.filesDir, fileName)
    file.delete()
}

fun deleteAllFiles(context: Context): Boolean {
    val dir = context.filesDir
    var success = true

    dir.listFiles()?.forEach { file ->
        if (!file.delete()) {
            success = false
        }
    }

    return success
}