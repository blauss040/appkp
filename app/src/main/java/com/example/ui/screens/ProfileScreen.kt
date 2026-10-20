package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Pet
import com.example.ui.theme.*
import com.example.viewmodel.AppUiState
import java.util.Locale

@Composable
fun ProfileScreen(
    uiState: AppUiState,
    onUpdateNotification: (String, Boolean) -> Unit,
    onUpdateThresholds: (Int, Int, Float) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectPet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Título
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Perfil & Configurações",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gerenciamento de tutor, coleiras e parâmetros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Mensagem de sucesso do relatório
        if (exportSuccessMessage != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KpGreenLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = KpGreenPrimary
                        )
                        Text(
                            text = exportSuccessMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KpGreenDark,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { exportSuccessMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = KpGreenDark)
                        }
                    }
                }
            }
        }

        // 1. Dados do Tutor & Assinatura
        item {
            TutorProfileCard()
        }

        // 2. Pets Vinculados & Dispositivos K&P
        item {
            LinkedPetsAndDevicesCard(
                pets = uiState.pets,
                onSelectPet = onSelectPet
            )
        }

        // 3. Gerenciar Notificações
        item {
            NotificationSettingsCard(
                notifications = uiState.notifications,
                onToggle = onUpdateNotification
            )
        }

        // 4. Configurar Limites de Alerta
        item {
            AlertLimitsCard(
                thresholds = uiState.thresholds,
                onUpdate = onUpdateThresholds
            )
        }

        // 5. Preferências de Idioma e Exportação
        item {
            SystemPreferencesCard(
                currentLanguage = uiState.selectedLanguage,
                onChangeLanguage = { showLanguageDialog = true },
                onExportReport = {
                    exportSuccessMessage = "Relatório Médico Veterinário K&P gerado com sucesso para compartilhamento!"
                }
            )
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = "Selecionar Idioma") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Português (Brasil)", "English (US)", "Español").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.selectedLanguage == lang,
                                onClick = {
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(text = "Fechar")
                }
            }
        )
    }
}

@Composable
private fun TutorProfileCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .testTag("tutor_profile_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(KpGreenUltralight)
                        .border(1.5.dp, KpGreenAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👩‍🦰", fontSize = 28.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Amanda Silva",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "amanda.silva@email.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+55 (11) 98765-4321",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Cartão do Plano
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KpGreenUltralight)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = KpGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Plano K&P Vitalidade Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = KpGreenDark
                        )
                        Text(
                            text = "Assinatura Anual • Até 5 coleiras ativas",
                            fontSize = 11.sp,
                            color = KpGreenDark.copy(alpha = 0.8f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(KpGreenPrimary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ativo",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkedPetsAndDevicesCard(
    pets: List<Pet>,
    onSelectPet: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pets & Coleiras Conectadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${pets.size} Dispositivos",
                    fontSize = 11.sp,
                    color = KpGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            pets.forEach { pet ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KpSurfaceVariant)
                        .clickable { onSelectPet(pet.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = pet.avatarEmoji, fontSize = 22.sp)
                        Column {
                            Text(
                                text = "${pet.name} (${pet.species})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Coleira: ${pet.collarId} • Firmware v3.2",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = if (pet.batteryPercent > 20) KpGreenPrimary else KpRedAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${pet.batteryPercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSettingsCard(
    notifications: com.example.viewmodel.NotificationSettings,
    onToggle: (String, Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .testTag("notification_settings_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Gerenciar Notificações",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            NotificationToggleRow(
                title = "Alerta de Batimentos Anormais",
                subtitle = "Avisar se frequência sair dos limites seguros",
                checked = notifications.heartRateAlerts,
                onCheckedChange = { onToggle("heartRate", it) }
            )

            NotificationToggleRow(
                title = "Saída da Cerca Virtual (Geofence)",
                subtitle = "Alerta sonoro imediato se o pet sair do raio",
                checked = notifications.fenceBoundaryAlerts,
                onCheckedChange = { onToggle("fence", it) }
            )

            NotificationToggleRow(
                title = "Temperatura Corporal Elevada",
                subtitle = "Avisar se temperatura exceder 39.2°C",
                checked = notifications.highTemperatureAlerts,
                onCheckedChange = { onToggle("temperature", it) }
            )

            NotificationToggleRow(
                title = "Relatório Diário de Saúde",
                subtitle = "Resumo dos sinais vitais todas as manhãs",
                checked = notifications.dailyDigestReport,
                onCheckedChange = { onToggle("digest", it) }
            )
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = KpGreenPrimary
            )
        )
    }
}

@Composable
private fun AlertLimitsCard(
    thresholds: com.example.viewmodel.AlertThresholds,
    onUpdate: (Int, Int, Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Configurar Limites de Alerta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Faixa Normal de Batimentos (BPM)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${thresholds.minBpm} - ${thresholds.maxBpm} BPM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KpGreenPrimary)
                }
                Slider(
                    value = thresholds.maxBpm.toFloat(),
                    onValueChange = { onUpdate(thresholds.minBpm, it.toInt(), thresholds.maxTemperatureCelsius) },
                    valueRange = 100f..180f,
                    colors = SliderDefaults.colors(thumbColor = KpGreenPrimary, activeTrackColor = KpGreenAccent)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Temperatura Máxima de Alerta", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format(Locale.US, "%.1f°C", thresholds.maxTemperatureCelsius),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KpYellowAccent
                    )
                }
                Slider(
                    value = thresholds.maxTemperatureCelsius,
                    onValueChange = { onUpdate(thresholds.minBpm, thresholds.maxBpm, it) },
                    valueRange = 38.5f..40.0f,
                    colors = SliderDefaults.colors(thumbColor = KpYellowWarning, activeTrackColor = KpYellowWarning)
                )
            }
        }
    }
}

@Composable
private fun SystemPreferencesCard(
    currentLanguage: String,
    onChangeLanguage: () -> Unit,
    onExportReport: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Geral & Suporte",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Trocar Idioma
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onChangeLanguage() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = KpBlueData
                    )
                    Text(
                        text = "Idioma do Aplicativo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentLanguage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Exportar Relatório Médico
            Button(
                onClick = onExportReport,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KpBlueData,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_veterinary_report_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exportar Relatório p/ Veterinário (PDF)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
