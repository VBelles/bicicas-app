package com.tcn.bicicas.stations.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tcn.bicicas.stations.domain.model.Station
import com.tcn.bicicas.main.theme.AvailableColor
import com.tcn.bicicas.main.theme.ElectricColor
import com.tcn.bicicas.main.theme.IncidentColor
import com.tcn.bicicas.main.theme.LocalDarkTheme
import com.tcn.bicicas.main.theme.MissingColor
import com.tcn.bicicas.main.theme.Theme
import com.tcn.bicicas.stations.presentation.station

@Composable
fun StationItem(
    station: Station,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    opacity: Float = 1f,
    onClick: (() -> Unit)? = null,
    onMapClicked: (() -> Unit)? = null,
    onFavoriteClicked: () -> Unit,
) {
    val lineHeight = with(LocalDensity.current) {
        MaterialTheme.typography.bodyLarge.lineHeight.toDp()
    }
    val collapsedHeight = lineHeight * 3
    val expandedHeight = collapsedHeight * 2
    val height by animateDpAsState(targetValue = if (expanded) expandedHeight else collapsedHeight)
    val borderWidth = 4.dp
    val borderWidthPx = with(LocalDensity.current) { borderWidth.toPx() }
    val isDarkTheme = LocalDarkTheme.current
    Surface(
        selected = false,
        tonalElevation = if (isDarkTheme) 1.dp else 0.dp,
        shadowElevation = if (opacity == 1f) 3.dp else 0.dp,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = opacity),
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = { onClick?.invoke() },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Column(modifier = Modifier.statusBorder(station, borderWidthPx)) {
            Row(
                modifier = Modifier.height(collapsedHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = station.id,
                    style =
                    MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleMedium.copy(lineHeight = MaterialTheme.typography.titleMedium.fontSize),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row {
                        Text(
                            text = "${station.bikesAvailable}/${station.anchors.size}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        if (station.electricBikesAvailable > 0) {
                            Icon(
                                rememberVectorPainter(image = Icons.Rounded.Bolt),
                                contentDescription = null
                            )
                        }
                    }
                }
                if (onMapClicked != null) {
                    IconButton(onClick = onMapClicked) {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = null,
                            tint = Color.Gray,
                        )
                    }
                }
                IconButton(onClick = onFavoriteClicked) {
                    Icons.Rounded.Favorite
                    val icon =
                        if (station.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (height > collapsedHeight) {
                StationItemDetails(Modifier.padding(start = borderWidth), station)
            }
        }
    }
}

@Composable
private fun StationItemDetails(modifier: Modifier = Modifier, station: Station) {
    // Row instead of LazyRow because of weird crashes
    Row(modifier.horizontalScroll(rememberScrollState())) {
        station.anchors.forEach { anchor ->
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(anchor.number.toString())
                AnchorImage(anchor)
                Text(anchor.bicycleId?.toString() ?: "")
            }
        }

    }
}

@Composable
fun StationItemVertical(
    station: Station,
    modifier: Modifier = Modifier,
    onFavoriteClicked: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .width(220.dp)
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = station.id,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.fontSize),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row {
                        Text(
                            text = "${station.bikesAvailable}/${station.anchors.size}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (station.electricBikesAvailable > 0) {
                            Icon(imageVector = Icons.Rounded.Bolt, contentDescription = null)
                        }
                    }
                }
                IconButton(onClick = onFavoriteClicked) {
                    val icon =
                        if (station.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(station.anchors, Station.Anchor::number) { anchor ->
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(anchor.number.toString(), modifier = Modifier.width(28.dp))
                        AnchorImage(anchor)
                        Spacer(Modifier.width(8.dp))
                        Text(anchor.bicycleId?.toString() ?: "")
                    }
                }

            }
        }
    }
}

@Composable
private fun AnchorImage(anchor: Station.Anchor) {
    val tint = when {
        anchor.hasIncident -> IncidentColor
        anchor.isElectric -> ElectricColor
        anchor.bicycleId != null -> AvailableColor
        else -> MissingColor
    }
    Image(
        imageVector = if (anchor.isElectric) Icons.Rounded.ElectricBike else Icons.Rounded.PedalBike,
        contentDescription = null,
        modifier = Modifier.rotate(-45f),
        colorFilter = ColorFilter.tint(tint)
    )
}

private fun Modifier.statusBorder(station: Station, strokeWidth: Float) = drawBehind {
    var offset = 0f
    drawLine(
        color = AvailableColor,
        start = Offset(strokeWidth / 2, 0f),
        end = Offset(strokeWidth / 2, size.height),
        strokeWidth = strokeWidth,
    )
    offset += (station.bikesAvailable.toFloat() / station.anchors.size) * size.height
    drawLine(
        color = IncidentColor,
        start = Offset(strokeWidth / 2, offset),
        end = Offset(strokeWidth / 2, offset + size.height),
        strokeWidth = strokeWidth,
    )
    offset += (station.incidents.toFloat() / station.anchors.size) * size.height
    drawLine(
        color = MissingColor,
        start = Offset(strokeWidth / 2, offset),
        end = Offset(strokeWidth / 2, offset + size.height),
        strokeWidth = strokeWidth,
    )

}


@Preview
@Composable
fun StationItemCollapsed() {
    Theme {
        Surface {
            StationItem(
                station = station(0),
                expanded = false,
                onFavoriteClicked = {})
        }
    }
}

@Preview
@Composable
fun StationItemExpanded() {
    Theme { Surface { StationItem(station = station(0), expanded = true, onFavoriteClicked = {}) } }
}

@Preview
@Composable
fun StationItemVertical() {
    Theme { Surface { StationItemVertical(station = station(0)) } }
}
