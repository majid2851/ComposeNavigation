package com.majid2851.navigator.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.majid2851.navigator.model.Destination
import com.majid2851.navigator.runtime.NavigatorController
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Visual styling knobs for [NavigationMap]. */
data class NavigationMapStyle(
    val nodeWidth: Dp = 168.dp,
    val nodeHeight: Dp = 84.dp,
    val horizontalGap: Dp = 96.dp,
    val verticalGap: Dp = 36.dp,
    val contentPadding: Dp = 48.dp,
    val minZoom: Float = 0.3f,
    val maxZoom: Float = 3f,
)

/**
 * Renders [controller]'s graph as an interactive, pannable, zoomable map — the "storyboard" view
 * that Jetpack Compose does not give you out of the box. Screens are nodes laid out left-to-right by
 * their distance from the start destination; transitions are arrows. The current screen and the
 * active back-stack path are highlighted, and tapping a node navigates to it.
 *
 * @param onNodeClick invoked when a node is tapped. Defaults to navigating to that route.
 */
@Composable
fun NavigationMap(
    controller: NavigatorController,
    modifier: Modifier = Modifier,
    style: NavigationMapStyle = NavigationMapStyle(),
    onNodeClick: (String) -> Unit = { controller.navigate(it, launchSingleTop = true) },
) {
    val edges = controller.edges
    val layout = remember(controller.graph, edges) {
        computeGraphLayout(controller.graph, edges)
    }
    val activePath = activePathEdges(controller.backStack)

    val density = LocalDensity.current
    val nodeWpx = with(density) { style.nodeWidth.toPx() }
    val nodeHpx = with(density) { style.nodeHeight.toPx() }
    val gapXpx = with(density) { style.horizontalGap.toPx() }
    val gapYpx = with(density) { style.verticalGap.toPx() }
    val padPx = with(density) { style.contentPadding.toPx() }

    val totalWidth = style.contentPadding * 2 +
        style.nodeWidth * layout.layerCount +
        style.horizontalGap * (layout.layerCount - 1).coerceAtLeast(0)
    val totalHeight = style.contentPadding * 2 +
        style.nodeHeight * layout.maxLayerSize +
        style.verticalGap * (layout.maxLayerSize - 1).coerceAtLeast(0)

    fun topLeft(layer: Int, index: Int): Offset = Offset(
        x = padPx + layer * (nodeWpx + gapXpx),
        y = padPx + index * (nodeHpx + gapYpx),
    )

    var scale by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    val edgeColor = MaterialTheme.colorScheme.outline
    val activeEdgeColor = MaterialTheme.colorScheme.primary
    val observedEdgeColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, panChange, zoomChange, _ ->
                    scale = (scale * zoomChange).coerceIn(style.minZoom, style.maxZoom)
                    pan += panChange
                }
            },
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = pan.x
                    translationY = pan.y
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .size(totalWidth, totalHeight),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val arrow = nodeHpx * 0.18f
                val strokePx = 2.dp.toPx()
                edges.forEach { edge ->
                    val from = layout.positions[edge.from] ?: return@forEach
                    val to = layout.positions[edge.to] ?: return@forEach
                    val fromTL = topLeft(from.layer, from.index)
                    val toTL = topLeft(to.layer, to.index)
                    val forward = to.layer >= from.layer
                    val start = Offset(
                        x = if (forward) fromTL.x + nodeWpx else fromTL.x,
                        y = fromTL.y + nodeHpx / 2f,
                    )
                    val end = Offset(
                        x = if (forward) toTL.x else toTL.x + nodeWpx,
                        y = toTL.y + nodeHpx / 2f,
                    )
                    val isActive = (edge.from to edge.to) in activePath
                    val color = when {
                        isActive -> activeEdgeColor
                        !edge.declared -> observedEdgeColor
                        else -> edgeColor
                    }
                    drawLine(
                        color = color,
                        start = start,
                        end = end,
                        strokeWidth = if (isActive) strokePx * 1.8f else strokePx,
                    )
                    drawArrowHead(end, start, color, arrow, if (isActive) strokePx * 1.8f else strokePx)
                }
            }

            controller.graph.destinations.values.forEach { destination ->
                val pos = layout.positions[destination.route] ?: return@forEach
                val tl = topLeft(pos.layer, pos.index)
                MapNode(
                    destination = destination,
                    isCurrent = destination.route == controller.currentRoute,
                    inBackStack = destination.route in controller.backStack,
                    isOrphan = destination.route !in layout.reachable,
                    width = style.nodeWidth,
                    height = style.nodeHeight,
                    modifier = Modifier.offset(
                        x = with(density) { tl.x.toDp() },
                        y = with(density) { tl.y.toDp() },
                    ),
                    onClick = { onNodeClick(destination.route) },
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowHead(
    tip: Offset,
    from: Offset,
    color: Color,
    size: Float,
    strokeWidth: Float,
) {
    val angle = atan2((tip.y - from.y).toDouble(), (tip.x - from.x).toDouble())
    val spread = Math.toRadians(28.0)
    val left = Offset(
        x = tip.x - (size * cos(angle - spread)).toFloat(),
        y = tip.y - (size * sin(angle - spread)).toFloat(),
    )
    val right = Offset(
        x = tip.x - (size * cos(angle + spread)).toFloat(),
        y = tip.y - (size * sin(angle + spread)).toFloat(),
    )
    drawLine(color, tip, left, strokeWidth)
    drawLine(color, tip, right, strokeWidth)
}

@Composable
private fun MapNode(
    destination: Destination,
    isCurrent: Boolean,
    inBackStack: Boolean,
    isOrphan: Boolean,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val container = when {
        isCurrent -> colors.primaryContainer
        inBackStack -> colors.secondaryContainer
        isOrphan -> colors.errorContainer
        else -> colors.surfaceVariant
    }
    val onContainer = when {
        isCurrent -> colors.onPrimaryContainer
        inBackStack -> colors.onSecondaryContainer
        isOrphan -> colors.onErrorContainer
        else -> colors.onSurfaceVariant
    }
    val borderColor = if (isCurrent) colors.primary else colors.outlineVariant

    Surface(
        modifier = modifier
            .size(width, height)
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp),
            ),
        shape = RoundedCornerShape(14.dp),
        color = container,
        contentColor = onContainer,
        onClick = onClick,
        tonalElevation = if (isCurrent) 6.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 10.dp)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                destination.icon?.let { icon ->
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = destination.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            destination.group?.let { group ->
                Text(
                    text = group,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = destination.route,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = onContainer.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
