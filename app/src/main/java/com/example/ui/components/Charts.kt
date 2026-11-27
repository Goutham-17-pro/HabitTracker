package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.DailyProductivityPoint

@Composable
fun ProductivityBarChart(
    points: List<DailyProductivityPoint>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(points) { mutableIntStateOf((points.size - 1).coerceAtLeast(0)) }
    val selected = points.getOrNull(selectedIndex)
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val track = MaterialTheme.colorScheme.surfaceContainerHighest

    Column(modifier = modifier.fillMaxWidth()) {
        if (selected != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selected.dayLabel} (${selected.dateKey.takeLast(5)})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${selected.completionPercent}% · ${selected.routineCompleted}/${selected.routineTotal} tasks",
                    style = MaterialTheme.typography.labelLarge,
                    color = primary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            points.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex
                val animatedRatio by animateFloatAsState(
                    targetValue = (point.completionPercent / 100f).coerceIn(0.04f, 1f),
                    animationSpec = tween(durationMillis = 550),
                    label = "bar_$index"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedIndex = index }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "${point.completionPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(104.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(track),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((104f * animatedRatio).dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (isSelected) {
                                            listOf(primary, secondary)
                                        } else {
                                            listOf(primary.copy(alpha = 0.65f), secondary.copy(alpha = 0.45f))
                                        }
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = point.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StudyTrendChart(
    points: List<DailyProductivityPoint>,
    dailyTargetMinutes: Int,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(points) { mutableIntStateOf((points.size - 1).coerceAtLeast(0)) }
    val selected = points.getOrNull(selectedIndex)
    val lineColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val maxMins = maxOf(
        dailyTargetMinutes,
        points.maxOfOrNull { it.studyMinutes } ?: 60,
        60
    ).toFloat()

    Column(modifier = modifier.fillMaxWidth()) {
        if (selected != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selected.dayLabel} Study Focus",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${selected.studyMinutes} min (Goal: ${dailyTargetMinutes}m)",
                    style = MaterialTheme.typography.labelLarge,
                    color = lineColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            if (points.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val stepX = if (points.size > 1) w / (points.size - 1) else w

            // Target dashed line
            val targetY = h - (dailyTargetMinutes / maxMins).coerceIn(0f, 1f) * h
            drawLine(
                color = tertiaryColor.copy(alpha = 0.65f),
                start = Offset(0f, targetY),
                end = Offset(w, targetY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )

            // Baseline grid
            drawLine(
                color = gridColor,
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 1.dp.toPx()
            )

            val coords = points.mapIndexed { idx, p ->
                val x = idx * stepX
                val y = h - (p.studyMinutes / maxMins).coerceIn(0f, 1f) * (h * 0.9f)
                Offset(x, y)
            }

            val linePath = Path().apply {
                coords.forEachIndexed { i, pt ->
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
            }
            val areaPath = Path().apply {
                addPath(linePath)
                lineTo(coords.last().x, h)
                lineTo(coords.first().x, h)
                close()
            }

            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.32f), Color.Transparent)
                )
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            coords.forEachIndexed { idx, pt ->
                val isSel = idx == selectedIndex
                if (isSel) {
                    drawCircle(
                        color = lineColor.copy(alpha = 0.25f),
                        radius = 11.dp.toPx(),
                        center = pt
                    )
                }
                drawCircle(
                    color = if (isSel) tertiaryColor else lineColor,
                    radius = if (isSel) 5.5.dp.toPx() else 4.dp.toPx(),
                    center = pt
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEachIndexed { index, p ->
                Text(
                    text = p.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == selectedIndex) lineColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedIndex = index }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryDonutChart(
    distribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val entries = distribution.entries.sortedByDescending { it.value }
    val total = entries.sumOf { it.value }
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val colors = entries.map { categoryAccentColor(it.key) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val stroke = 16.dp.toPx()
                val inset = stroke / 2f
                val arcSize = Size(size.width - stroke, size.height - stroke)
                if (total == 0) {
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                } else {
                    var startAngle = -90f
                    entries.forEachIndexed { idx, entry ->
                        val sweep = (entry.value.toFloat() / total.toFloat()) * 360f
                        drawArc(
                            color = colors[idx],
                            startAngle = startAngle,
                            sweepAngle = (sweep - 3f).coerceAtLeast(1f),
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        startAngle += sweep
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Blocks",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (entries.isEmpty()) {
                Text(
                    text = "Add routine activities to see your focus breakdown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                entries.forEachIndexed { idx, entry ->
                    val pct = if (total == 0) 0 else (entry.value * 100) / total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colors[idx])
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = entry.key,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${entry.value} ($pct%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
