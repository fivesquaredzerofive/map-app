package com.wanderer.repetitortap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun MarkerColorPickerPopup(
    initialColor: Color,
    onColorCommitted: (Color) -> Unit,
    onDismissRequest: () -> Unit
) {
    var previewColor by remember(initialColor) { mutableStateOf(initialColor) }
    var previewPosition by remember(initialColor) { mutableStateOf<Offset?>(null) }
    var gradientSize by remember { mutableStateOf(IntSize.Zero) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 164.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .onSizeChanged { gradientSize = it }
                    .pointerInput(initialColor) {
                        awaitEachGesture {
                            if (gradientSize == IntSize.Zero) return@awaitEachGesture

                            val down = awaitFirstDown(requireUnconsumed = false)
                            var currentPosition = down.position.clampToBounds(gradientSize)

                            previewPosition = currentPosition
                            previewColor = colorAtPosition(currentPosition, gradientSize)

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                currentPosition = change.position.clampToBounds(gradientSize)
                                previewPosition = currentPosition
                                previewColor = colorAtPosition(currentPosition, gradientSize)
                                change.consume()

                                if (!change.pressed) {
                                    onColorCommitted(previewColor)
                                    onDismissRequest()
                                    break
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Green,
                                Color.Cyan,
                                Color.Blue,
                                Color.Magenta,
                                Color.Red
                            )
                        )
                    )

                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.White,
                                0.5f to Color.Transparent,
                                1f to Color.Black
                            )
                        )
                    )

                    previewPosition?.let { position ->
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.35f),
                            radius = 10.dp.toPx(),
                            center = position,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 10.dp.toPx(),
                            center = position,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ColorPreviewSwatch(
                    color = previewColor,
                    modifier = Modifier.weight(1f)
                )
                ColorPreviewSwatch(
                    color = initialColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ColorPreviewSwatch(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp)
            )
    )
}

private fun Offset.clampToBounds(size: IntSize): Offset {
    return Offset(
        x = x.coerceIn(0f, size.width.toFloat()),
        y = y.coerceIn(0f, size.height.toFloat())
    )
}

private fun colorAtPosition(
    position: Offset,
    size: IntSize
): Color {
    if (size.width == 0 || size.height == 0) return Color.White

    val xRatio = (position.x / size.width).coerceIn(0f, 1f)
    val yRatio = (position.y / size.height).coerceIn(0f, 1f)
    val hueColor = Color.hsv(
        hue = xRatio * 360f,
        saturation = 1f,
        value = 1f
    )

    return if (yRatio <= 0.5f) {
        lerp(Color.White, hueColor, yRatio / 0.5f)
    } else {
        lerp(hueColor, Color.Black, (yRatio - 0.5f) / 0.5f)
    }
}
