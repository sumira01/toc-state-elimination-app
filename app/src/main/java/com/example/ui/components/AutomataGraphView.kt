package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DfaModel
import com.example.model.GnfaModel
import com.example.model.RegexNode
import com.example.ui.theme.EliminatingHighlightColor
import com.example.ui.theme.GnfaAcceptColor
import com.example.ui.theme.GnfaStartColor
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class VisualEdge(
    val from: String,
    val to: String,
    val label: String
)

@Composable
fun AutomataGraphView(
    dfa: DfaModel? = null,
    gnfa: GnfaModel? = null,
    eliminatingState: String? = null,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3.5f)
                    offset += pan
                }
            }
            .testTag("automata_graph_view")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (canvasWidth <= 0 || canvasHeight <= 0) return@Canvas

            // Extract states & edges
            val states: List<String>
            val startState: String
            val finalStates: Set<String>
            val edges: List<VisualEdge>
            val isGnfa = gnfa != null

            if (gnfa != null) {
                states = gnfa.states
                startState = gnfa.startState
                finalStates = setOf(gnfa.acceptState)
                edges = gnfa.getActiveTransitions().map {
                    VisualEdge(from = it.from, to = it.to, label = it.label.toString())
                }
            } else if (dfa != null) {
                states = dfa.states
                startState = dfa.startState
                finalStates = dfa.finalStates
                // Group DFA transitions between same u->v
                val map = mutableMapOf<Pair<String, String>, MutableList<String>>()
                for ((key, target) in dfa.transitions) {
                    val (u, sym) = key
                    map.getOrPut(Pair(u, target)) { mutableListOf() }.add(sym)
                }
                edges = map.map { (pair, syms) ->
                    VisualEdge(from = pair.first, to = pair.second, label = syms.joinToString(","))
                }
            } else {
                return@Canvas
            }

            if (states.isEmpty()) return@Canvas

            // Compute positions
            val positions = computeNodePositions(
                states = states,
                startState = startState,
                acceptState = if (isGnfa) GnfaModel.ACCEPT_STATE else null,
                width = canvasWidth,
                height = canvasHeight
            )

            val nodeRadius = 32.dp.toPx() * scale

            // Draw grid dots for subtle tech texture
            drawGrid(scale, offset, onSurfaceColor.copy(alpha = 0.08f))

            // Draw Edges
            for (edge in edges) {
                val fromPos = positions[edge.from]?.let { it * scale + offset } ?: continue
                val toPos = positions[edge.to]?.let { it * scale + offset } ?: continue

                val isOppositeEdgePresent = edges.any { it.from == edge.to && it.to == edge.from && edge.from != edge.to }
                val isSelfLoop = edge.from == edge.to

                if (isSelfLoop) {
                    drawSelfLoop(
                        center = fromPos,
                        radius = nodeRadius,
                        label = edge.label,
                        color = primaryColor,
                        labelColor = onSurfaceColor,
                        bgColor = surfaceColor,
                        scale = scale
                    )
                } else {
                    drawDirectedEdge(
                        from = fromPos,
                        to = toPos,
                        radius = nodeRadius,
                        label = edge.label,
                        isCurved = isOppositeEdgePresent,
                        color = if (edge.label == "ε") secondaryColor else primaryColor,
                        labelColor = onSurfaceColor,
                        bgColor = surfaceColor,
                        scale = scale
                    )
                }
            }

            // Draw Nodes
            for (state in states) {
                val center = positions[state]?.let { it * scale + offset } ?: continue
                val isStart = state == startState
                val isFinal = finalStates.contains(state)
                val isEliminating = state == eliminatingState

                val nodeBgColor = when {
                    isEliminating -> EliminatingHighlightColor.copy(alpha = 0.25f)
                    isGnfa && state == GnfaModel.START_STATE -> GnfaStartColor.copy(alpha = 0.2f)
                    isGnfa && state == GnfaModel.ACCEPT_STATE -> GnfaAcceptColor.copy(alpha = 0.2f)
                    isStart -> primaryColor.copy(alpha = 0.15f)
                    else -> surfaceColor
                }

                val nodeBorderColor = when {
                    isEliminating -> EliminatingHighlightColor
                    isGnfa && state == GnfaModel.START_STATE -> GnfaStartColor
                    isGnfa && state == GnfaModel.ACCEPT_STATE -> GnfaAcceptColor
                    isStart -> primaryColor
                    else -> outlineColor
                }

                // Node circle
                drawCircle(
                    color = nodeBgColor,
                    radius = nodeRadius,
                    center = center
                )
                drawCircle(
                    color = nodeBorderColor,
                    radius = nodeRadius,
                    center = center,
                    style = Stroke(width = if (isEliminating) 3.5.dp.toPx() else 2.5.dp.toPx())
                )

                // Double ring for final states
                if (isFinal) {
                    drawCircle(
                        color = nodeBorderColor,
                        radius = nodeRadius - 6.dp.toPx() * scale,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Start arrow (if start state and not special GNFA node)
                if (isStart && !isGnfa) {
                    drawStartArrow(center, nodeRadius, primaryColor)
                }

                // Text label inside node
                drawNodeLabel(
                    text = state,
                    center = center,
                    textColor = onSurfaceColor,
                    scale = scale
                )
            }
        }

        // Overlay Controls: Zoom in, Zoom out, Reset
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                IconButton(
                    onClick = { scale = (scale * 1.25f).coerceAtMost(3.5f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { scale = (scale / 1.25f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset View",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Mode badge at top-left
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = if (gnfa != null) GnfaAcceptColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (gnfa != null) GnfaAcceptColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = if (gnfa != null) "GNFA Graph (${gnfa.states.size} states)" else "DFA Graph (${dfa?.states?.size ?: 0} states)",
                style = MaterialTheme.typography.labelMedium,
                color = if (gnfa != null) GnfaAcceptColor else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun computeNodePositions(
    states: List<String>,
    startState: String,
    acceptState: String?,
    width: Float,
    height: Float
): Map<String, Offset> {
    val map = mutableMapOf<String, Offset>()
    val padding = 70.dp.value
    val centerX = width / 2f
    val centerY = height / 2f

    if (acceptState != null && states.contains(GnfaModel.START_STATE) && states.contains(GnfaModel.ACCEPT_STATE)) {
        // GNFA pipeline layout:
        // q_start at left, q_accept at right, intermediate states in circle/oval between them
        val leftX = padding + 20f
        val rightX = width - padding - 20f
        map[GnfaModel.START_STATE] = Offset(leftX, centerY)
        map[GnfaModel.ACCEPT_STATE] = Offset(rightX, centerY)

        val intermediate = states.filter { it != GnfaModel.START_STATE && it != GnfaModel.ACCEPT_STATE }
        if (intermediate.size == 1) {
            map[intermediate.first()] = Offset(centerX, centerY)
        } else if (intermediate.isNotEmpty()) {
            val rx = (rightX - leftX) / 2f - 60f
            val ry = (centerY - padding) * 0.75f
            for ((index, state) in intermediate.withIndex()) {
                val angle = (2 * PI * index / intermediate.size) - (PI / 2)
                val x = centerX + rx * cos(angle).toFloat()
                val y = centerY + ry * sin(angle).toFloat()
                map[state] = Offset(x, y)
            }
        }
    } else {
        // DFA layout: circular or horizontal chain
        if (states.size == 1) {
            map[states.first()] = Offset(centerX, centerY)
        } else if (states.size == 2) {
            map[states[0]] = Offset(centerX - 130f, centerY)
            map[states[1]] = Offset(centerX + 130f, centerY)
        } else {
            val radius = minOf(centerX - padding, centerY - padding) * 0.75f
            // Put start state at left-most angle (PI)
            val startIndex = states.indexOf(startState).let { if (it >= 0) it else 0 }
            for ((index, state) in states.withIndex()) {
                val angleOffset = PI
                val angle = angleOffset + (2 * PI * (index - startIndex) / states.size)
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                map[state] = Offset(x, y)
            }
        }
    }
    return map
}

private fun DrawScope.drawGrid(scale: Float, offset: Offset, dotColor: Color) {
    val step = 36f * scale
    val startX = (offset.x % step)
    val startY = (offset.y % step)
    var x = startX
    while (x < size.width) {
        var y = startY
        while (y < size.height) {
            drawCircle(dotColor, radius = 1.5f, center = Offset(x, y))
            y += step
        }
        x += step
    }
}

private fun DrawScope.drawStartArrow(center: Offset, radius: Float, color: Color) {
    val startPoint = Offset(center.x - radius - 45f, center.y)
    val endPoint = Offset(center.x - radius, center.y)
    drawLine(color = color, start = startPoint, end = endPoint, strokeWidth = 3f)

    // Arrowhead
    val path = Path().apply {
        moveTo(endPoint.x, endPoint.y)
        lineTo(endPoint.x - 14f, endPoint.y - 7f)
        lineTo(endPoint.x - 14f, endPoint.y + 7f)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawSelfLoop(
    center: Offset,
    radius: Float,
    label: String,
    color: Color,
    labelColor: Color,
    bgColor: Color,
    scale: Float
) {
    val loopRadius = radius * 0.8f
    val loopCenter = Offset(center.x, center.y - radius - loopRadius * 0.6f)

    drawCircle(
        color = color,
        radius = loopRadius,
        center = loopCenter,
        style = Stroke(width = 2.5f)
    )

    // Arrowhead near right base of loop
    val arrowX = center.x + radius * 0.45f
    val arrowY = center.y - radius * 0.85f
    val path = Path().apply {
        moveTo(arrowX, arrowY)
        lineTo(arrowX + 10f, arrowY - 12f)
        lineTo(arrowX - 2f, arrowY - 12f)
        close()
    }
    drawPath(path, color)

    // Label on top
    drawPillLabel(
        text = label,
        center = Offset(loopCenter.x, loopCenter.y - loopRadius - 8f),
        textColor = labelColor,
        bgColor = bgColor,
        borderColor = color,
        scale = scale
    )
}

private fun DrawScope.drawDirectedEdge(
    from: Offset,
    to: Offset,
    radius: Float,
    label: String,
    isCurved: Boolean,
    color: Color,
    labelColor: Color,
    bgColor: Color,
    scale: Float
) {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val dist = sqrt(dx * dx + dy * dy)
    if (dist < 1f) return

    val angle = atan2(dy, dx)
    val curveOffset = if (isCurved) 36f * scale else 0f

    // Midpoint with curve offset perpendicular to line
    val midX = (from.x + to.x) / 2f - curveOffset * sin(angle)
    val midY = (from.y + to.y) / 2f + curveOffset * cos(angle)
    val controlPoint = Offset(midX, midY)

    val path = Path()
    if (isCurved) {
        // Curve from edge of 'from' node to edge of 'to' node
        val startX = from.x + radius * cos(angle + 0.25f)
        val startY = from.y + radius * sin(angle + 0.25f)
        val endX = to.x - radius * cos(angle - 0.25f)
        val endY = to.y - radius * sin(angle - 0.25f)

        path.moveTo(startX, startY)
        path.quadraticTo(controlPoint.x, controlPoint.y, endX, endY)
        drawPath(path, color, style = Stroke(width = 2.5f))

        // Arrowhead at endX, endY
        val arrowAngle = atan2(endY - controlPoint.y, endX - controlPoint.x)
        drawArrowHead(Offset(endX, endY), arrowAngle, color)

        // Pill label at curve crest
        drawPillLabel(label, controlPoint, labelColor, bgColor, color, scale)
    } else {
        val startX = from.x + radius * cos(angle)
        val startY = from.y + radius * sin(angle)
        val endX = to.x - radius * cos(angle)
        val endY = to.y - radius * sin(angle)

        drawLine(color = color, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 2.5f)
        drawArrowHead(Offset(endX, endY), angle, color)

        val mid = Offset((startX + endX) / 2f, (startY + endY) / 2f - 14f)
        drawPillLabel(label, mid, labelColor, bgColor, color, scale)
    }
}

private fun DrawScope.drawArrowHead(tip: Offset, angle: Float, color: Color) {
    val arrowLen = 14f
    val arrowAngle = PI.toFloat() / 7f

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(
            tip.x - arrowLen * cos(angle - arrowAngle),
            tip.y - arrowLen * sin(angle - arrowAngle)
        )
        lineTo(
            tip.x - arrowLen * cos(angle + arrowAngle),
            tip.y - arrowLen * sin(angle + arrowAngle)
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawNodeLabel(
    text: String,
    center: Offset,
    textColor: Color,
    scale: Float
) {
    val paint = Paint().apply {
        this.color = textColor.toArgb()
        this.textSize = (14f * scale).coerceIn(12f, 22f)
        this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        this.textAlign = Paint.Align.CENTER
        this.isAntiAlias = true
    }

    val fontMetrics = paint.fontMetrics
    val yPos = center.y - (fontMetrics.ascent + fontMetrics.descent) / 2f
    drawContext.canvas.nativeCanvas.drawText(text, center.x, yPos, paint)
}

private fun DrawScope.drawPillLabel(
    text: String,
    center: Offset,
    textColor: Color,
    bgColor: Color,
    borderColor: Color,
    scale: Float
) {
    val paint = Paint().apply {
        this.color = textColor.toArgb()
        this.textSize = (11f * scale).coerceIn(10f, 18f)
        this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        this.textAlign = Paint.Align.CENTER
        this.isAntiAlias = true
    }

    val textWidth = paint.measureText(text)
    val fontMetrics = paint.fontMetrics
    val textHeight = fontMetrics.descent - fontMetrics.ascent

    val padX = 8f
    val padY = 4f
    val pillWidth = textWidth + padX * 2
    val pillHeight = textHeight + padY * 2

    // Background pill
    val left = center.x - pillWidth / 2f
    val top = center.y - pillHeight / 2f

    drawRoundRect(
        color = bgColor,
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = borderColor.copy(alpha = 0.5f),
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(width = 1f)
    )

    val yPos = center.y - (fontMetrics.ascent + fontMetrics.descent) / 2f
    drawContext.canvas.nativeCanvas.drawText(text, center.x, yPos, paint)
}
