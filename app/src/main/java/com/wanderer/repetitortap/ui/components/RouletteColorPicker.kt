package com.wanderer.repetitortap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun RouletteColorPicker(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .onSizeChanged { size = it }
            .pointerInput(colors, size) {
                detectTapGestures { offset ->
                    if (size == IntSize.Zero || colors.isEmpty()) return@detectTapGestures

                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f
                    val segmentAngle = 2 * PI / colors.size
                    val swatchRadius = radius * 0.2f
                    var closestIndex = -1
                    var closestDistance = Float.MAX_VALUE

                    for (i in colors.indices) {
                        val angle = i * segmentAngle
                        val cx = center.x + (radius * 0.75f) * cos(angle).toFloat()
                        val cy = center.y + (radius * 0.75f) * sin(angle).toFloat()
                        val distance = sqrt(
                            (offset.x - cx) * (offset.x - cx) +
                                (offset.y - cy) * (offset.y - cy)
                        )

                        if (distance < closestDistance) {
                            closestDistance = distance
                            closestIndex = i
                        }
                    }

                    if (closestIndex >= 0 && closestDistance <= swatchRadius * 1.5f) {
                        onColorSelected(colors[closestIndex])
                    }
                }
            }
    ) {
        // Draw the outer ring of colors
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f
            
            // Draw ring track background (One UI style)
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.2f),
                radius = radius,
                center = center
            )
            
            drawCircle(
                color = Color.White,
                radius = radius * 0.5f,
                center = center
            )

            // Draw color circles on the ring
            val segmentAngle = 2 * PI / colors.size
            val selectedArgb = selectedColor.toArgb()
            for (i in colors.indices) {
                val angle = i * segmentAngle
                val cx = center.x + (radius * 0.75f) * cos(angle).toFloat()
                val cy = center.y + (radius * 0.75f) * sin(angle).toFloat()
                
                val isSelected = colors[i].toArgb() == selectedArgb
                val circleRadius = if (isSelected) radius * 0.2f else radius * 0.15f
                
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = circleRadius + 4.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }

                drawCircle(
                    color = colors[i],
                    radius = circleRadius,
                    center = Offset(cx, cy)
                )
            }
        }

        // Center preview
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(selectedColor)
        )
    }
}
