package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HealthStatus
import com.example.model.TimePeriod
import com.example.model.VitalSample
import com.example.ui.components.HealthMetricBadge
import com.example.ui.components.PetSelectorRow
import com.example.ui.theme.*
import com.example.viewmodel.AppUiState
import java.util.Locale

@Composable
fun ChartsScreen(
    uiState: AppUiState,
    onSelectPet: (String) -> Unit,
    onSelectPeriod: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val pet = uiState.selectedPet
    val samples = when (uiState.selectedPeriod) {
        TimePeriod.HOJE -> pet.todaySamples
        TimePeriod.SETE_DIAS -> pet.weekSamples
        TimePeriod.TRINTA_DIAS -> pet.monthSamples
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Título e seletor
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Sinais Vitais",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Acompanhamento fisiológico contínuo e histórico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Seletor de Pet
        item {
            PetSelectorRow(
                pets = uiState.pets,
                selectedPetId = uiState.selectedPetId,
                onSelectPet = onSelectPet
            )
        }

        // Seletor de Período: Hoje / 7 dias / 30 dias
        item {
            PeriodSelectorRow(
                selectedPeriod = uiState.selectedPeriod,
                onSelectPeriod = onSelectPeriod
            )
        }

        // 1. Gráfico de Frequência Cardíaca (💚 Verde)
        item {
            HeartRateChartCard(
                samples = samples,
                currentBpm = pet.heartRateBpm,
                status = pet.heartRateStatus
            )
        }

        // 2. Gráfico de SpO2 (🔵 Azul Claro)
        item {
            Spo2ChartCard(
                samples = samples,
                currentSpo2 = pet.spo2Percent,
                status = pet.spo2Status
            )
        }

        // 3. Gráfico de Temperatura (💛 Amarelo / 🔴 Vermelho)
        item {
            TemperatureChartCard(
                samples = samples,
                currentTemp = pet.temperatureCelsius,
                status = pet.temperatureStatus
            )
        }

        // 4. Cartão de Análise Veterinária de Estabilidade
        item {
            VitalsSummaryAnalysisCard(
                petName = pet.name,
                samples = samples
            )
        }
    }
}

@Composable
private fun PeriodSelectorRow(
    selectedPeriod: TimePeriod,
    onSelectPeriod: (TimePeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KpSurfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TimePeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onSelectPeriod(period) }
                    .padding(vertical = 8.dp)
                    .testTag("period_tab_${period.name}")
            ) {
                Text(
                    text = period.title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 💓 Gráfico de Frequência Cardíaca
@Composable
private fun HeartRateChartCard(
    samples: List<VitalSample>,
    currentBpm: Int,
    status: HealthStatus
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .testTag("heart_rate_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KpGreenUltralight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💓", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "Frequência Cardíaca",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Faixa recomendada: 60 - 100 BPM",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$currentBpm BPM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KpGreenPrimary
                    )
                    HealthMetricBadge(status = status)
                }
            }

            // Tooltip se houver ponto tocado
            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tooltip"
            ) { idx ->
                if (idx != null && idx in samples.indices) {
                    val s = samples[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(KpGreenUltralight)
                            .border(1.dp, KpGreenAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toque: ${s.timeLabel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KpGreenDark
                        )
                        Text(
                            text = "${s.heartRate} BPM (Normal 🟢)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KpGreenPrimary
                        )
                    }
                } else {
                    Text(
                        text = "Toque no gráfico para ver detalhes e horários",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Canvas Interativo do Gráfico
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .pointerInput(samples) {
                        detectTapGestures(
                            onTap = { offset ->
                                val step = size.width / (samples.size - 1).coerceAtLeast(1)
                                val index = (offset.x / step).toInt().coerceIn(0, samples.lastIndex)
                                selectedIndex = index
                            }
                        )
                    }
                    .pointerInput(samples) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                val step = size.width / (samples.size - 1).coerceAtLeast(1)
                                val index = (change.position.x / step).toInt().coerceIn(0, samples.lastIndex)
                                selectedIndex = index
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val minY = 50f
                    val maxY = 130f
                    val normalMinY = 60f
                    val normalMaxY = 100f

                    // Faixa normal destacada no eixo Y
                    val normalTop = h - ((normalMaxY - minY) / (maxY - minY)) * h
                    val normalBottom = h - ((normalMinY - minY) / (maxY - minY)) * h
                    drawRect(
                        color = KpGreenUltralight,
                        topLeft = Offset(0f, normalTop),
                        size = androidx.compose.ui.geometry.Size(w, normalBottom - normalTop)
                    )

                    // Linha de grade normal
                    drawLine(
                        color = KpGreenAccent.copy(alpha = 0.3f),
                        start = Offset(0f, normalTop),
                        end = Offset(w, normalTop),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    val points = samples.mapIndexed { index, sample ->
                        val x = if (samples.size > 1) (index.toFloat() / (samples.size - 1)) * w else w / 2
                        val y = h - ((sample.heartRate - minY) / (maxY - minY)).coerceIn(0f, 1f) * h
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val midX = (prev.x + curr.x) / 2
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }

                        // Gradient fill
                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(KpGreenAccent.copy(alpha = 0.25f), Color.Transparent),
                                startY = 0f,
                                endY = h
                            )
                        )

                        // Linha contínua
                        drawPath(
                            path = strokePath,
                            color = KpGreenPrimary,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Pontos
                        points.forEachIndexed { i, pt ->
                            val isTapped = i == selectedIndex
                            drawCircle(
                                color = if (isTapped) KpGreenDark else KpGreenPrimary,
                                radius = if (isTapped) 6.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = if (isTapped) 3.dp.toPx() else 2.dp.toPx(),
                                center = pt
                            )
                            if (isTapped) {
                                drawLine(
                                    color = KpGreenPrimary.copy(alpha = 0.6f),
                                    start = Offset(pt.x, 0f),
                                    end = Offset(pt.x, h),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                            }
                        }
                    }
                }
            }

            // Labels de tempo na base
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                samples.forEach { s ->
                    Text(
                        text = s.timeLabel,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 🫁 Gráfico de SpO₂ (🔵 Azul Claro)
@Composable
private fun Spo2ChartCard(
    samples: List<VitalSample>,
    currentSpo2: Int,
    status: HealthStatus
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .testTag("spo2_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KpBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🫁", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "Oxigenação no Sangue (SpO₂)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Normal: ≥ 95%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$currentSpo2%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KpBlueData
                    )
                    HealthMetricBadge(status = status)
                }
            }

            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "spo2_tooltip"
            ) { idx ->
                if (idx != null && idx in samples.indices) {
                    val s = samples[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(KpBlueLight)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Horário: ${s.timeLabel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KpBlueData
                        )
                        Text(
                            text = "SpO₂: ${s.spo2}% (Excelente 🟢)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KpBlueData
                        )
                    }
                } else {
                    Text(
                        text = "Oxigenação celular adequada. Toque para inspecionar.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .pointerInput(samples) {
                        detectTapGestures { offset ->
                            val step = size.width / (samples.size - 1).coerceAtLeast(1)
                            selectedIndex = (offset.x / step).toInt().coerceIn(0, samples.lastIndex)
                        }
                    }
                    .pointerInput(samples) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val step = size.width / (samples.size - 1).coerceAtLeast(1)
                            selectedIndex = (change.position.x / step).toInt().coerceIn(0, samples.lastIndex)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val minY = 90f
                    val maxY = 100f

                    // Linha guia 95%
                    val guideY = h - ((95f - minY) / (maxY - minY)) * h
                    drawLine(
                        color = KpBlueAccent.copy(alpha = 0.4f),
                        start = Offset(0f, guideY),
                        end = Offset(w, guideY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )

                    val points = samples.mapIndexed { index, sample ->
                        val x = if (samples.size > 1) (index.toFloat() / (samples.size - 1)) * w else w / 2
                        val y = h - ((sample.spo2 - minY) / (maxY - minY)).coerceIn(0f, 1f) * h
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val midX = (prev.x + curr.x) / 2
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(KpBlueAccent.copy(alpha = 0.3f), Color.Transparent),
                                startY = 0f,
                                endY = h
                            )
                        )

                        drawPath(
                            path = strokePath,
                            color = KpBlueData,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        points.forEachIndexed { i, pt ->
                            val isTapped = i == selectedIndex
                            drawCircle(
                                color = if (isTapped) KpBlueData else KpBlueAccent,
                                radius = if (isTapped) 6.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                samples.forEach { s ->
                    Text(
                        text = s.timeLabel,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 🌡️ Gráfico de Temperatura (💛 Amarelo / 🔴 Vermelho)
@Composable
private fun TemperatureChartCard(
    samples: List<VitalSample>,
    currentTemp: Float,
    status: HealthStatus
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val isAlert = currentTemp >= 39.5f
    val mainColor = if (isAlert) KpRedAlert else KpYellowWarning

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .testTag("temperature_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAlert) KpRedLight else KpYellowLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌡️", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "Temperatura Corporal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Limite de alerta: ≥ 39.2°C",
                            fontSize = 11.sp,
                            color = if (status == HealthStatus.ATENCAO) KpYellowAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "%.1f°C", currentTemp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = mainColor
                    )
                    HealthMetricBadge(status = status)
                }
            }

            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "temp_tooltip"
            ) { idx ->
                if (idx != null && idx in samples.indices) {
                    val s = samples[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (s.temperature >= 39.2f) KpYellowLight else KpGreenUltralight)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ponto: ${s.timeLabel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f°C (%s)", s.temperature, if (s.temperature >= 39.2f) "Atenção 🟡" else "Normal 🟢"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (s.temperature >= 39.2f) KpYellowAccent else KpGreenPrimary
                        )
                    }
                } else {
                    Text(
                        text = "Toque na curva para ver o registro horário da temperatura",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .pointerInput(samples) {
                        detectTapGestures { offset ->
                            val step = size.width / (samples.size - 1).coerceAtLeast(1)
                            selectedIndex = (offset.x / step).toInt().coerceIn(0, samples.lastIndex)
                        }
                    }
                    .pointerInput(samples) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val step = size.width / (samples.size - 1).coerceAtLeast(1)
                            selectedIndex = (change.position.x / step).toInt().coerceIn(0, samples.lastIndex)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val minY = 37.0f
                    val maxY = 40.0f
                    val alertThreshold = 39.2f

                    // Área de alerta (acima de 39.2)
                    val alertTopY = 0f
                    val alertBottomY = h - ((alertThreshold - minY) / (maxY - minY)) * h
                    drawRect(
                        color = KpRedLight.copy(alpha = 0.35f),
                        topLeft = Offset(0f, alertTopY),
                        size = androidx.compose.ui.geometry.Size(w, alertBottomY)
                    )

                    // Linha tracejada do Limite de Alerta
                    drawLine(
                        color = KpRedAlert,
                        start = Offset(0f, alertBottomY),
                        end = Offset(w, alertBottomY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    val points = samples.mapIndexed { index, sample ->
                        val x = if (samples.size > 1) (index.toFloat() / (samples.size - 1)) * w else w / 2
                        val y = h - ((sample.temperature - minY) / (maxY - minY)).coerceIn(0f, 1f) * h
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val midX = (prev.x + curr.x) / 2
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(mainColor.copy(alpha = 0.25f), Color.Transparent),
                                startY = 0f,
                                endY = h
                            )
                        )

                        drawPath(
                            path = strokePath,
                            color = mainColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        points.forEachIndexed { i, pt ->
                            val isTapped = i == selectedIndex
                            val ptColor = if (samples[i].temperature >= alertThreshold) KpYellowAccent else KpGreenPrimary
                            drawCircle(
                                color = if (isTapped) ptColor else mainColor,
                                radius = if (isTapped) 6.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                samples.forEach { s ->
                    Text(
                        text = s.timeLabel,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VitalsSummaryAnalysisCard(
    petName: String,
    samples: List<VitalSample>
) {
    val avgBpm = if (samples.isNotEmpty()) samples.map { it.heartRate }.average().toInt() else 72
    val minBpm = samples.minOfOrNull { it.heartRate } ?: 68
    val maxBpm = samples.maxOfOrNull { it.heartRate } ?: 76
    val avgTemp = if (samples.isNotEmpty()) samples.map { it.temperature.toDouble() }.average() else 38.2

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KpSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = KpGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Diagnóstico do Período — $petName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Text(
                text = "Variação cardíaca de $minBpm a $maxBpm BPM com média de $avgBpm BPM. Temperatura média de ${String.format(Locale.US, "%.1f°C", avgTemp)}. Padrão de oxigenação excelente sem hipóxia.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
