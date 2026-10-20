package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HealthStatus
import com.example.model.Pet
import com.example.model.PetStatus
import com.example.ui.components.AlertBanner
import com.example.ui.components.HealthMetricBadge
import com.example.ui.components.LocationSafetyBadge
import com.example.ui.components.PetStatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.AppUiState

@Composable
fun HomeScreen(
    uiState: AppUiState,
    onSelectPet: (String) -> Unit,
    onNavigateToCharts: (String) -> Unit,
    onNavigateToMap: (String) -> Unit,
    onNavigateToSayori: () -> Unit,
    onDismissAlert: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header com Saudação personalizada e status do sistema
        item {
            HomeGreetingHeader(
                totalReadings = uiState.totalReadingsToday,
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh
            )
        }

        // 2. Alerta ativo (se houver)
        item {
            AnimatedVisibility(visible = uiState.activeAlert != null) {
                uiState.activeAlert?.let { alert ->
                    AlertBanner(
                        alert = alert,
                        onDismiss = onDismissAlert
                    )
                }
            }
        }

        // 3. Resumo Rápido (3 métricas chave)
        item {
            QuickSummarySection(
                petsCount = uiState.pets.size,
                readingsCount = uiState.totalReadingsToday,
                activeAlerts = uiState.activeAlertCount
            )
        }

        // 4. Seção: Cards dos Pets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meus Pets Monitorados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Coleira K&P Ativa",
                    style = MaterialTheme.typography.labelSmall,
                    color = KpGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Cards rápidos de cada pet
        items(uiState.pets, key = { it.id }) { pet ->
            PetQuickCard(
                pet = pet,
                onClick = {
                    onSelectPet(pet.id)
                    onNavigateToCharts(pet.id)
                },
                onViewCharts = {
                    onSelectPet(pet.id)
                    onNavigateToCharts(pet.id)
                },
                onViewMap = {
                    onSelectPet(pet.id)
                    onNavigateToMap(pet.id)
                }
            )
        }

        // 5. Card Promocional/Atalho para Sayori
        item {
            SayoriQuickBanner(onOpenSayori = onNavigateToSayori)
        }
    }
}

@Composable
private fun HomeGreetingHeader(
    totalReadings: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(KpGreenAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SISTEMA VITAL EM TEMPO REAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = KpGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Olá, Amanda! 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Seus pets estão protegidos hoje",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FilledTonalIconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(44.dp)
                .testTag("home_refresh_button"),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = KpGreenUltralight,
                contentColor = KpGreenPrimary
            )
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = KpGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Atualizar telemetria"
                )
            }
        }
    }
}

@Composable
private fun QuickSummarySection(
    petsCount: Int,
    readingsCount: Int,
    activeAlerts: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Pets protegidos
        SummaryMetricCard(
            label = "Pets ativos",
            value = "$petsCount",
            icon = Icons.Default.Pets,
            accentColor = KpGreenPrimary,
            bgColor = KpGreenUltralight,
            modifier = Modifier.weight(1f)
        )

        // Leituras hoje
        SummaryMetricCard(
            label = "Leituras hoje",
            value = "$readingsCount",
            icon = Icons.Default.Sensors,
            accentColor = KpBlueData,
            bgColor = KpBlueUltralight,
            modifier = Modifier.weight(1f)
        )

        // Alertas ativos
        SummaryMetricCard(
            label = "Alertas ativos",
            value = if (activeAlerts > 0) "$activeAlerts atenção" else "0",
            icon = Icons.Default.NotificationsActive,
            accentColor = if (activeAlerts > 0) KpYellowAccent else KpGreenPrimary,
            bgColor = if (activeAlerts > 0) KpYellowUltralight else KpGreenUltralight,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PetQuickCard(
    pet: Pet,
    onClick: () -> Unit,
    onViewCharts: () -> Unit,
    onViewMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("pet_card_${pet.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Topo do Card: Foto/Ícone + Nome + Raça + Bateria + Status Visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(KpGreenLight, KpBlueLight)
                                )
                            )
                    ) {
                        Text(text = pet.avatarEmoji, fontSize = 24.sp)
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pet.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            PetStatusBadge(status = pet.currentStatus)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${pet.breed} • ${pet.age}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Indicador de bateria da coleira
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(KpSurfaceVariant)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Bateria",
                        tint = if (pet.batteryPercent > 20) KpGreenPrimary else KpRedAlert,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${pet.batteryPercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // 📐 4 — ESTRUTURA DE DADOS EXIBIDOS
            // Cada pet mostra:
            // ├─ 💓 Batimento: 72 BPM   (🟢 Normal)
            // ├─ 🫁 Oxigenação: 98%      (🟢 Normal)
            // ├─ 🌡️ Temperatura: 38.2°C  (💛 Atenção)
            // └─ 📍 Localização: Dentro da área segura — 10:45
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                // Batimento
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "💓", fontSize = 14.sp)
                        Text(
                            text = "Batimento:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${pet.heartRateBpm} BPM",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HealthMetricBadge(status = pet.heartRateStatus)
                }

                // Oxigenação (SpO2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🫁", fontSize = 14.sp)
                        Text(
                            text = "Oxigenação:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${pet.spo2Percent}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HealthMetricBadge(status = pet.spo2Status)
                }

                // Temperatura
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🌡️", fontSize = 14.sp)
                        Text(
                            text = "Temperatura:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${pet.temperatureCelsius}°C",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pet.temperatureStatus == HealthStatus.ATENCAO) KpYellowAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HealthMetricBadge(status = pet.temperatureStatus)
                }

                // Localização
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📍", fontSize = 14.sp)
                    Text(
                        text = "Localização:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LocationSafetyBadge(
                    status = pet.locationStatus,
                    timeText = pet.lastUpdate
                )
            }

            // Ações rápidas no rodapé do card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewMap,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pet_view_map_btn_${pet.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = KpBlueData
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ver no Mapa", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onViewCharts,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pet_view_charts_btn_${pet.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KpGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ver Gráficos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SayoriQuickBanner(onOpenSayori: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = KpGreenUltralight),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, KpGreenAccent.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .clickable { onOpenSayori() }
            .testTag("sayori_quick_banner")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(KpGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👩‍⚕️", fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dúvidas sobre os sinais vitais?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KpGreenDark,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "A Sayori analisa batimentos, temperatura e alertas em tempo real.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KpGreenDark.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Falar com Sayori",
                tint = KpGreenPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
