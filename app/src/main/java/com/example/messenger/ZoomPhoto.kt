package com.example.messenger

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.max
import kotlin.math.min

@Composable
fun ZoomableImage(
    path: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        // Загружаем bitmap из файла
        val bitmap = remember(path) {
            BitmapFactory.decodeFile(path)
        } ?: return@Dialog  // если не загрузилось — выйдем

        var scale by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier.fillMaxSize()
                .background(color = Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale < 1.1f) {
                                scale = 2.5f
                            } else {
                                scale = 1f
                            }
                            offsetX = 0f
                            offsetY = 0f
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        val fitCoef = min(
                            size.width.toFloat() / bitmap.width,
                            size.height.toFloat() / bitmap.height
                        )

                        val imgWidth = bitmap.width * fitCoef * scale
                        val imgHeight = bitmap.height * fitCoef * scale

                        val halfContainerW = size.width / 2f
                        val halfContainerH = size.height / 2f

                        val halfImgW = imgWidth / 2f
                        val halfImgH = imgHeight / 2f

                        val maxX = max(0f, halfImgW - halfContainerW)
                        val maxY = max(0f, halfImgH - halfContainerH)

                        val nX = offsetX + pan.x
                        val nY = offsetY + pan.y

                        offsetX = nX.coerceIn(-maxX, maxX)
                        offsetY = nY.coerceIn(-maxY, maxY)
                    }
                }
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            )
        }
    }
}
