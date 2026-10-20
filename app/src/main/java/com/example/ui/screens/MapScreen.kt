package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationBreadcrumb
import com.example.model.LocationSafetyStatus
import com.example.model.Pet
import com.example.ui.components.LocationSafetyBadge
import com.example.ui.components.PetSelectorRow
import com.example.ui.theme.*
import com.example.viewmodel.AppUiState

@Composable
fun MapScreen(
    uiState: AppUiState,
    onSelectPet: (String) -> Unit,
    onUpdateSafeRadius: (Float) -> Unit,
    onToggleSimulateExit: (String) -> Unit,
    onToggleHistory: () -> Unit,
    onCenterPet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pet = uiState.selectedPet

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Cerca Virtual & Localização",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Perímetro inteligente com GPS de alta precisão",
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

        // Alerta sonoro/visual em destaque se o pet estiver fora
        item {
            AnimatedVisibility(visible = pet.locationStatus == LocationSafetyStatus.FORA_PERIMETRO) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KpRedLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, KpRedAlert, RoundedCornerShape(16.dp))
                        .testTag("geofence_alarm_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(KpRedAlert),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🚨 ALERTA: ${pet.name} SAIU DO PERÍMETRO!",
                                fontWeight = FontWeight.Bold,
                                color = KpRedDark,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Distância atual: ${pet.currentDistanceMeters.toInt()}m da base segura. Notificação e vibração ativadas.",
                                fontSize = 12.sp,
                                color = KpRedDark.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Visualização do Mapa com Cerca Virtual
        item {
            MapCanvasCard(
                pet = pet,
                showHistory = uiState.showLocationHistory,
                onCenterPet = onCenterPet,
                onToggleHistory = onToggleHistory,
                onToggleSimulateExit = { onToggleSimulateExit(pet.id) }
            )
        }

        // Legenda simples embaixo do mapa
        item {
            MapColorLegend()
        }

        // Controle de Ajuste do Raio da Cerca
        item {
            FenceRadiusControllerCard(
                safeRadiusMeters = pet.safeRadiusMeters,
                onRadiusChanged = onUpdateSafeRadius
            )
        }

        // Card de Telemetria e Endereço em Tempo Real
        item {
            PetLocationDetailsCard(pet = pet)
        }

        // Histórico de locais visitados (se ativado)
        if (uiState.showLocationHistory && pet.breadcrumbs.isNotEmpty()) {
            item {
                BreadcrumbsListCard(breadcrumbs = pet.breadcrumbs)
            }
        }
    }
}

@Composable
private fun MapCanvasCard(
    pet: Pet,
    showHistory: Boolean,
    onCenterPet: () -> Unit,
    onToggleHistory: () -> Unit,
    onToggleSimulateExit: () -> Unit
) {
    // Animação de pulso do radar
    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            .testTag("map_canvas_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            // Desenho do Mapa no Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f
                val centerY = h / 2f

                // 1. Fundo do Mapa (Asfalto e Terrenos Suaves)
                drawRect(color = Color(0xFFF1F5F9))

                // Blocos urbanos / Quarteirões
                val blockColor = Color(0xFFE2E8F0)
                drawRect(color = blockColor, topLeft = Offset(20f, 20f), size = Size(w * 0.40f, h * 0.28f))
                drawRect(color = blockColor, topLeft = Offset(w * 0.55f, 20f), size = Size(w * 0.40f, h * 0.28f))
                drawRect(color = blockColor, topLeft = Offset(20f, h * 0.65f), size = Size(w * 0.40f, h * 0.30f))
                drawRect(color = blockColor, topLeft = Offset(w * 0.55f, h * 0.65f), size = Size(w * 0.40f, h * 0.30f))

                // Área Verde / Parque
                val parkColor = Color(0xFFDCFCE7)
                drawRoundRect(
                    color = parkColor,
                    topLeft = Offset(w * 0.60f, centerY - 45f),
                    size = Size(w * 0.35f, 90f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )

                // Ruas (Linhas brancas limpas)
                val streetColor = Color.White
                // Rua horizontal central
                drawRect(color = streetColor, topLeft = Offset(0f, centerY - 20f), size = Size(w, 40f))
                // Rua vertical central
                drawRect(color = streetColor, topLeft = Offset(centerX - 20f, 0f), size = Size(40f, h))

                // Faixas centrais pontilhadas das vias
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(0f, centerY),
                    end = Offset(w, centerY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, h),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )

                // 2. CERCA VIRTUAL
                // Raio proporcional no canvas (escala base: 150m ~ 110dp)
                val scale = 0.75f
                val safeRadiusPx = pet.safeRadiusMeters * scale
                val attentionRadiusPx = safeRadiusPx * 1.15f

                // 🔴 Fora do perímetro: Se o pet estiver fora, sutil vinheta de perigo
                if (pet.locationStatus == LocationSafetyStatus.FORA_PERIMETRO) {
                    drawRect(color = KpRedAlert.copy(alpha = 0.08f))
                }

                // 💛 Zona de atenção — borda da cerca (amarelo translúcido)
                drawCircle(
                    color = KpYellowWarning.copy(alpha = 0.18f),
                    radius = attentionRadiusPx,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = KpYellowWarning,
                    radius = attentionRadiusPx,
                    center = Offset(centerX, centerY),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                )

                // 🟢 Área permitida — zona segura (cor verde translúcida)
                drawCircle(
                    color = KpGreenAccent.copy(alpha = 0.22f),
                    radius = safeRadiusPx,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = KpGreenPrimary,
                    radius = safeRadiusPx,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Base / Casa do Tutor (Centro da Cerca)
                drawCircle(color = Color.White, radius = 16.dp.toPx(), center = Offset(centerX, centerY))
                drawCircle(color = KpGreenPrimary, radius = 14.dp.toPx(), center = Offset(centerX, centerY))
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(centerX, centerY))

                // 3. Histórico de Rota (Breadcrumbs)
                if (showHistory && pet.breadcrumbs.isNotEmpty()) {
                    val path = Path()
                    val breadcrumbPoints = pet.breadcrumbs.map { b ->
                        Offset(
                            x = centerX + (b.lngOffset * safeRadiusPx * 1.8f),
                            y = centerY + (b.latOffset * safeRadiusPx * 1.8f)
                        )
                    }

                    if (breadcrumbPoints.isNotEmpty()) {
                        path.moveTo(breadcrumbPoints.first().x, breadcrumbPoints.first().y)
                        for (i in 1 until breadcrumbPoints.size) {
                            path.lineTo(breadcrumbPoints[i].x, breadcrumbPoints[i].y)
                        }

                        // Linha tracejada da rota
                        drawPath(
                            path = path,
                            color = KpBlueData.copy(alpha = 0.7f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        )

                        // Pontos do histórico
                        breadcrumbPoints.forEach { pt ->
                            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                            drawCircle(color = KpBlueData, radius = 3.5.dp.toPx(), center = pt)
                        }
                    }
                }

                // 4. POSIÇÃO ATUAL DO PET — Marcador animado
                val petX = centerX + (pet.currentPetPosNormalizedX * safeRadiusPx * 1.4f)
                val petY = centerY + (pet.currentPetPosNormalizedY * safeRadiusPx * 1.4f)
                val petOffset = Offset(petX, petY)

                val pulseColor = when (pet.locationStatus) {
                    LocationSafetyStatus.DENTRO_AREA_SEGURA -> KpGreenAccent
                    LocationSafetyStatus.BORDA_ATENCAO -> KpYellowWarning
                    LocationSafetyStatus.FORA_PERIMETRO -> KpRedAlert
                }

                // Pulso de radar animado
                drawCircle(
                    color = pulseColor.copy(alpha = pulseAlpha),
                    radius = pulseRadius.dp.toPx(),
                    center = petOffset
                )

                // Halo do marcador
                drawCircle(
                    color = Color.White,
                    radius = 16.dp.toPx(),
                    center = petOffset
                )
                drawCircle(
                    color = pulseColor,
                    radius = 14.dp.toPx(),
                    center = petOffset
                )
            }

            // Elemento UI sobreposto no marcador com emoji do pet
            // Badge flutuante sobre a base "Casa (Base)"
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-26).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(1.dp, KpBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🏠 Base Segura",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = KpGreenDark
                )
            }

            // Botões de Ação Rápida no Canto Superior do Mapa
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Centralizar no Pet
                FilledTonalIconButton(
                    onClick = onCenterPet,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("center_on_pet_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White,
                        contentColor = KpGreenPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Centralizar no Pet",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Alternar Histórico de Locais
                FilledTonalIconButton(
                    onClick = onToggleHistory,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("toggle_history_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (showHistory) KpBlueData else Color.White,
                        contentColor = if (showHistory) Color.White else KpBlueData
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = "Histórico de Locais",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Botão de Teste / Simulação de Saída da Cerca no Canto Inferior Esquerdo
            Button(
                onClick = onToggleSimulateExit,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pet.locationStatus == LocationSafetyStatus.FORA_PERIMETRO) KpGreenPrimary else KpYellowWarning,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .testTag("simulate_fence_exit_button")
            ) {
                Text(
                    text = if (pet.locationStatus == LocationSafetyStatus.FORA_PERIMETRO) "🟢 Voltar à Área Segura" else "⚡ Simular Saída de Cerca",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 🟢 Legenda simples embaixo do mapa explicando cada cor
@Composable
private fun MapColorLegend() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Verde
            LegendItem(
                dotColor = KpGreenAccent,
                label = "Área permitida",
                subLabel = "Zona Segura"
            )

            // Amarelo
            LegendItem(
                dotColor = KpYellowWarning,
                label = "Zona de atenção",
                subLabel = "Borda da Cerca"
            )

            // Vermelho
            LegendItem(
                dotColor = KpRedAlert,
                label = "Fora do perímetro",
                subLabel = "Área de Alerta"
            )
        }
    }
}

@Composable
private fun LegendItem(
    dotColor: Color,
    label: String,
    subLabel: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subLabel,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Controle do Raio da Cerca
@Composable
private fun FenceRadiusControllerCard(
    safeRadiusMeters: Float,
    onRadiusChanged: (Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .testTag("fence_radius_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KpGreenUltralight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Adjust,
                            contentDescription = null,
                            tint = KpGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Definir Raio da Cerca Virtual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "${safeRadiusMeters.toInt()} metros",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = KpGreenPrimary
                )
            }

            Slider(
                value = safeRadiusMeters,
                onValueChange = onRadiusChanged,
                valueRange = 50f..500f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = KpGreenPrimary,
                    activeTrackColor = KpGreenAccent,
                    inactiveTrackColor = KpBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("safe_radius_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "50m (Casa)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "150m (Pátio/Rua)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "500m (Bairro)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Card de Detalhes da Localização Atual
@Composable
private fun PetLocationDetailsCard(pet: Pet) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Telemetria & Posição Atual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            LocationSafetyBadge(
                status = pet.locationStatus,
                timeText = pet.lastUpdate
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Distância da base", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${pet.currentDistanceMeters.toInt()} metros",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(text = "Precisão do GPS", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "± 1.2 m (Satélite 3D)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KpGreenPrimary
                    )
                }

                Column {
                    Text(text = "Sinal da Coleira", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = pet.signalStrength,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = KpBlueData,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = pet.addressDescription,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// Histórico de Locais Visitados
@Composable
private fun BreadcrumbsListCard(breadcrumbs: List<LocationBreadcrumb>) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
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
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = KpBlueData,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Histórico de Locais Visitados Hoje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            breadcrumbs.forEach { crumb ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(KpGreenPrimary)
                        )
                        Text(
                            text = crumb.address,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = crumb.time,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
