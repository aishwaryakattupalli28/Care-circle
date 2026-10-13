package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WeeklyVitalPoint
import com.example.ui.theme.*

@Composable
fun WeeklyVitalsTrendVisualizer(
    weeklyPoints: List<WeeklyVitalPoint>,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(0) } // 0: Heart Rate, 1: SpO2, 2: Blood Pressure, 3: Temperature
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val metricColors = listOf(
        AccentCoral,     // Heart Rate
        ElectricBlue,    // SpO2
        SoftPurple,      // BP
        AccentAmber      // Temp
    )

    val currentThemeColor = metricColors[selectedMetric]

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.ShowChart,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Weekly Vitals Trend Summary",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        "Recharts-style multi-metric 7-day trend telemetry",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NavySurface)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("7 DAYS", color = PrimaryTealLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Metric Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf("Heart Rate", "SpO2 %", "BP (mmHg)", "Temp °C")
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedMetric == index
                    val tabColor = metricColors[index]

                    Surface(
                        onClick = {
                            selectedMetric = index
                            selectedPointIndex = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) tabColor.copy(alpha = 0.2f) else NavySurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) tabColor else NavyBorder
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) tabColor else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Active Metric Metrics & Selected Point Tooltip
            val activePoint = if (selectedPointIndex != null && selectedPointIndex in weeklyPoints.indices) {
                weeklyPoints[selectedPointIndex!!]
            } else {
                weeklyPoints.lastOrNull()
            }

            if (activePoint != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${activePoint.dayLabel}: " + when (selectedMetric) {
                            0 -> "${activePoint.heartRateAvg.toInt()} bpm (Min ${activePoint.heartRateMin.toInt()} - Max ${activePoint.heartRateMax.toInt()})"
                            1 -> "${activePoint.spo2Avg.toInt()}% Avg SpO2"
                            2 -> "${activePoint.bpSysAvg.toInt()} / ${activePoint.bpDiaAvg.toInt()} mmHg"
                            else -> "${activePoint.tempAvg}°C Avg Temp"
                        },
                        color = currentThemeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text("Tap days below to inspect", color = TextTertiary, fontSize = 10.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // The Canvas Trend Chart (Smooth bezier spline + gradient fill + axis lines)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                RechartsTrendCanvas(
                    points = weeklyPoints,
                    selectedMetric = selectedMetric,
                    lineColor = currentThemeColor,
                    selectedPointIndex = selectedPointIndex,
                    onSelectPoint = { selectedPointIndex = it }
                )
            }

            Spacer(Modifier.height(6.dp))

            // Day Labels Axis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyPoints.forEachIndexed { index, point ->
                    Text(
                        text = point.dayLabel,
                        color = if (selectedPointIndex == index) currentThemeColor else TextTertiary,
                        fontWeight = if (selectedPointIndex == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { selectedPointIndex = index }
                    )
                }
            }
        }
    }
}

@Composable
fun RechartsTrendCanvas(
    points: List<WeeklyVitalPoint>,
    selectedMetric: Int,
    lineColor: Color,
    selectedPointIndex: Int?,
    onSelectPoint: (Int) -> Unit
) {
    if (points.isEmpty()) return

    // Extract values based on metric
    val values = points.map { pt ->
        when (selectedMetric) {
            0 -> pt.heartRateAvg
            1 -> pt.spo2Avg
            2 -> pt.bpSysAvg
            else -> pt.tempAvg
        }
    }

    val minVal = (values.minOrNull() ?: 60f) * 0.92f
    val maxVal = (values.maxOrNull() ?: 100f) * 1.08f
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1).coerceAtLeast(1)

        // Draw horizontal grid lines (Recharts style)
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = Color(0xFF22364B),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Compute point coordinates
        val coords = values.mapIndexed { idx, v ->
            val norm = (v - minVal) / range
            val y = height - (norm * (height - 30.dp.toPx())) - 15.dp.toPx()
            val x = idx * stepX
            Offset(x, y)
        }

        // Build smooth curved path
        val strokePath = Path()
        val fillPath = Path()

        coords.forEachIndexed { i, pt ->
            if (i == 0) {
                strokePath.moveTo(pt.x, pt.y)
                fillPath.moveTo(pt.x, height)
                fillPath.lineTo(pt.x, pt.y)
            } else {
                val prev = coords[i - 1]
                val control1 = Offset((prev.x + pt.x) / 2f, prev.y)
                val control2 = Offset((prev.x + pt.x) / 2f, pt.y)
                strokePath.cubicTo(control1.x, control1.y, control2.x, control2.y, pt.x, pt.y)
                fillPath.cubicTo(control1.x, control1.y, control2.x, control2.y, pt.x, pt.y)
            }
        }

        // Close gradient fill
        fillPath.lineTo(coords.last().x, height)
        fillPath.close()

        // Draw area gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw continuous stroke line
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw data points and active tooltip rings
        coords.forEachIndexed { idx, pt ->
            val isSelected = selectedPointIndex == idx

            drawCircle(
                color = NavyBackground,
                radius = if (isSelected) 8.dp.toPx() else 4.5.dp.toPx(),
                center = pt
            )

            drawCircle(
                color = if (isSelected) Color.White else lineColor,
                radius = if (isSelected) 6.dp.toPx() else 3.dp.toPx(),
                center = pt
            )

            if (isSelected) {
                drawCircle(
                    color = lineColor.copy(alpha = 0.4f),
                    radius = 12.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}
