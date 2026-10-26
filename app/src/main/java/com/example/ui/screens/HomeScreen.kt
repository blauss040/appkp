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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    uiState: AppUiState,
    onSelectPet: (String) -> Unit,
    onNavigateToCharts: (String) -> Unit,
    onNavigateToMap: (String) -> Unit,
    onNavigateToSayori: () -> Unit,
    onDismissAlert: () -> Unit,
    onRefresh: () -> Unit,
    onActivateCollar: (suspend (String, String, String) -> Result<String>)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
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

        // 4. Seção e Formulário de Ativação da Coleira K&P
        item {
            CollarActivationSection(
                pets = uiState.pets,
                onActivateCollar = onActivateCollar
            )
        }

        // 5. Seção: Cards dos Pets
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

    // Bolha Flutuante de Chat da Sayori (Floating Chat Bubble)
    ExtendedFloatingActionButton(
        onClick = onNavigateToSayori,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 16.dp)
            .testTag("floating_sayori_chat_fab"),
        containerColor = KpGreenPrimary,
        contentColor = Color.White,
        shape = CircleShape,
        icon = {
            Text(text = "👩‍⚕️", fontSize = 20.sp)
        },
        text = {
            Column {
                Text(
                    text = "Sayori IA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Online • Dúvidas",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    )
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
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (pet.collarId.isNotEmpty()) KpBlueLight else KpSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "Coleira",
                                tint = if (pet.collarId.isNotEmpty()) KpBlueData else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (pet.collarId.isNotEmpty()) "Coleira: ${pet.collarId}" else "Sem coleira",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (pet.collarId.isNotEmpty()) KpBlueData else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

enum class CollarStatusState {
    NOT_ACTIVATED,
    ACTIVATING,
    ACTIVATED,
    ERROR
}

private data class StatusCardConfig(
    val bg: Color,
    val border: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String
)

@Composable
fun CollarActivationSection(
    pets: List<Pet>,
    onActivateCollar: (suspend (String, String, String) -> Result<String>)?,
    modifier: Modifier = Modifier
) {
    var serialNumber by remember { mutableStateOf("") }
    var activationPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedPetId by remember(pets) { mutableStateOf(pets.firstOrNull()?.id ?: "") }
    var activationStatus by remember { mutableStateOf(CollarStatusState.NOT_ACTIVATED) }
    var statusMessage by remember { mutableStateOf("Coleira pendente de ativação no sistema.") }
    val coroutineScope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .testTag("collar_activation_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header da Seção
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KpGreenUltralight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Ativar Coleira",
                            tint = KpGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Ativação de Coleira K&P",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vincule a coleira inteligente ao seu pet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Badge de Status Geral
                val (badgeBg, badgeText, badgeColor) = when (activationStatus) {
                    CollarStatusState.ACTIVATED -> Triple(KpGreenLight, "Ativada", KpGreenDark)
                    CollarStatusState.ACTIVATING -> Triple(KpYellowLight, "Ativando...", KpYellowAccent)
                    CollarStatusState.ERROR -> Triple(KpRedLight, "Erro", KpRedDark)
                    CollarStatusState.NOT_ACTIVATED -> Triple(KpSurfaceVariant, "Não ativada", MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Campo 1: Número de Série
            OutlinedTextField(
                value = serialNumber,
                onValueChange = { serialNumber = it.uppercase() },
                label = { Text("Número de Série da Coleira") },
                placeholder = { Text("Ex: KP-COLLAR-8921") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = KpGreenPrimary)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("collar_serial_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KpGreenPrimary,
                    focusedLabelColor = KpGreenPrimary
                )
            )

            // Campo 2: Senha de Ativação
            OutlinedTextField(
                value = activationPassword,
                onValueChange = { activationPassword = it },
                label = { Text("Senha de Ativação (PIN)") },
                placeholder = { Text("Chave de 6 dígitos") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = KpGreenPrimary)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("collar_password_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KpGreenPrimary,
                    focusedLabelColor = KpGreenPrimary
                )
            )

            // Campo 3: Selecionar Pet Vinculado
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Selecionar Pet Vinculado:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pets.forEach { pet ->
                        val isSelected = pet.id == selectedPetId
                        val chipBg = if (isSelected) KpGreenPrimary else MaterialTheme.colorScheme.surfaceVariant
                        val chipTextColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .clickable { selectedPetId = pet.id }
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                                .testTag("select_pet_chip_${pet.id}")
                        ) {
                            Text(text = pet.avatarEmoji, fontSize = 16.sp)
                            Text(
                                text = pet.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipTextColor
                            )
                        }
                    }
                }
            }

            // Botão de Envio para a API
            Button(
                onClick = {
                    if (serialNumber.trim().length < 4) {
                        activationStatus = CollarStatusState.ERROR
                        statusMessage = "Informe um Número de Série válido com ao menos 4 caracteres."
                        return@Button
                    }
                    if (activationPassword.trim().length < 4) {
                        activationStatus = CollarStatusState.ERROR
                        statusMessage = "A Senha de Ativação deve conter ao menos 4 dígitos."
                        return@Button
                    }
                    if (selectedPetId.isEmpty()) {
                        activationStatus = CollarStatusState.ERROR
                        statusMessage = "Selecione o pet que usará esta coleira."
                        return@Button
                    }

                    isSubmitting = true
                    activationStatus = CollarStatusState.ACTIVATING
                    statusMessage = "Validando e enviando dados para a API K&P..."

                    coroutineScope.launch {
                        val result = onActivateCollar?.invoke(selectedPetId, serialNumber, activationPassword)
                            ?: Result.success("Coleira ativada com sucesso!")

                        isSubmitting = false
                        if (result.isSuccess) {
                            activationStatus = CollarStatusState.ACTIVATED
                            statusMessage = result.getOrNull() ?: "Coleira ativada e vinculada com sucesso!"
                            serialNumber = ""
                            activationPassword = ""
                        } else {
                            activationStatus = CollarStatusState.ERROR
                            statusMessage = result.exceptionOrNull()?.message ?: "Falha ao ativar coleira."
                        }
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KpGreenPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_activate_collar")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conectando à API...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ativar e Vincular Coleira", fontWeight = FontWeight.Bold)
                }
            }

            // Exibição de Status: Ativada / Não ativada / Erro
            val config = when (activationStatus) {
                CollarStatusState.ACTIVATED -> StatusCardConfig(
                    KpGreenLight,
                    KpGreenAccent,
                    Icons.Default.CheckCircle,
                    "Status: Ativada"
                )
                CollarStatusState.ERROR -> StatusCardConfig(
                    KpRedLight,
                    KpRedAlert,
                    Icons.Default.Error,
                    "Status: Erro"
                )
                CollarStatusState.ACTIVATING -> StatusCardConfig(
                    KpYellowLight,
                    KpYellowWarning,
                    Icons.Default.HourglassTop,
                    "Status: Processando"
                )
                CollarStatusState.NOT_ACTIVATED -> StatusCardConfig(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.outline,
                    Icons.Default.Info,
                    "Status: Não ativada"
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(config.bg)
                    .border(1.dp, config.border, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("activation_status_feedback")
            ) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    tint = config.border,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = config.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = statusMessage,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

