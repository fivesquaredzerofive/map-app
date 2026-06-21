package com.wanderer.repetitortap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.wanderer.repetitortap.data.MarkerEntity
import kotlin.math.roundToInt
import androidx.compose.foundation.ExperimentalFoundationApi

@Composable
fun DrawerContent(
    markers: List<MarkerEntity>,
    onMarkerClick: (MarkerEntity) -> Unit,
    onMarkerDelete: (MarkerEntity) -> Unit,
    onMarkerRename: (MarkerEntity, String) -> Unit,
    onMarkerColorChange: (MarkerEntity, Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.8f)
            .widthIn(min = 180.dp)
            .background(MaterialTheme.colorScheme.surface),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Interactive Maps",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 12.dp)
            )

            Text(
                text = "Saved Markers",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = markers,
                    key = { it.id }
                ) { marker ->
                    MarkerListItem(
                        marker = marker,
                        onClick = { onMarkerClick(marker) },
                        onDelete = { onMarkerDelete(marker) },
                        onTitleCommit = { title -> onMarkerRename(marker, title) },
                        onColorCommit = { color -> onMarkerColorChange(marker, color) }
                    )
                }

                if (markers.isEmpty()) {
                    item {
                        Text(
                            text = "No markers saved yet.\nTap anywhere on the map to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSettingsClick)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun MarkerListItem(
    marker: MarkerEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTitleCommit: (String) -> Unit,
    onColorCommit: (Long) -> Unit
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember(marker.id) { FocusRequester() }

    var isEditingTitle by remember(marker.id) { mutableStateOf(false) }
    var draftTitle by remember(marker.id, marker.title) { mutableStateOf(marker.title) }
    var titleFieldWasFocused by remember(marker.id) { mutableStateOf(false) }
    var isColorPickerVisible by remember(marker.id) { mutableStateOf(false) }
    var colorAnchorOffset by remember(marker.id) { mutableStateOf(IntOffset.Zero) }
    var colorAnchorSize by remember(marker.id) { mutableStateOf(IntSize.Zero) }

    fun commitTitle() {
        val committedTitle = draftTitle.trim().ifBlank { marker.title }
        draftTitle = committedTitle
        isEditingTitle = false
        titleFieldWasFocused = false
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        onTitleCommit(committedTitle)
    }

    LaunchedEffect(isEditingTitle) {
        if (isEditingTitle) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(enabled = !isEditingTitle, onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInWindow()
                        colorAnchorOffset = IntOffset(
                            x = position.x.roundToInt(),
                            y = position.y.roundToInt()
                        )
                        colorAnchorSize = coordinates.size
                    }
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { isColorPickerVisible = true }
                    )
                    .background(Color(marker.color.toInt()))
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (isEditingTitle) {
                BasicTextField(
                    value = draftTitle,
                    onValueChange = { draftTitle = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                titleFieldWasFocused = true
                            } else if (isEditingTitle && titleFieldWasFocused) {
                                commitTitle()
                            }
                        }
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                                commitTitle()
                                true
                            } else {
                                false
                            }
                        },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { commitTitle() })
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            draftTitle = marker.title
                            titleFieldWasFocused = false
                            isEditingTitle = true
                        }
                ) {
                    Text(
                        text = marker.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete marker",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    if (isColorPickerVisible) {
        val popupOffset = with(density) {
            IntOffset(
                x = colorAnchorOffset.x + colorAnchorSize.width + 12.dp.roundToPx(),
                y = colorAnchorOffset.y - 12.dp.roundToPx()
            )
        }

        Popup(
            alignment = Alignment.TopStart,
            offset = popupOffset,
            onDismissRequest = { isColorPickerVisible = false },
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                clippingEnabled = false
            )
        ) {
            MarkerColorPickerPopup(
                initialColor = Color(marker.color.toInt()),
                onColorCommitted = { color ->
                    onColorCommit(color.toArgb().toLong() and 0xFFFFFFFFL)
                    isColorPickerVisible = false
                },
                onDismissRequest = { isColorPickerVisible = false }
            )
        }
    }
}
