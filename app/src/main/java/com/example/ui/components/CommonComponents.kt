package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HealthStatus
import com.example.model.LocationSafetyStatus
import com.example.model.Pet
import com.example.model.PetStatus
import com.example.ui.theme.*
import com.example.viewmodel.UiAlert

@Composable
fun PetStatusBadge(
    status: PetStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderCol) = when (status) {
        PetStatus.OK -> Triple(KpGreenUltralight, KpGreenDark, KpGreenAccent.copy(alpha = 0.4f))
        PetStatus.ATENCAO -> Triple(KpYellowUltralight, KpYellowAccent, KpYellowWarning.copy(alpha = 0.5f))
        PetStatus.ALERTA -> Triple(KpRedLight, KpRedDark, KpRedAlert.copy(alpha = 0.6f))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("pet_status_badge_${status.name}")
    ) {
        Text(text = status.emoji, fontSize = 11.sp)
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun HealthMetricBadge(
    status: HealthStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        HealthStatus.NORMAL -> Pair(KpGreenLight, KpGreenDark)
        HealthStatus.ATENCAO -> Pair(KpYellowLight, KpYellowAccent)
        HealthStatus.ALERTA -> Pair(KpRedLight, KpRedDark)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = status.emoji, fontSize = 10.sp)
        Text(
            text = status.label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LocationSafetyBadge(
    status: LocationSafetyStatus,
    timeText: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when (status) {
        LocationSafetyStatus.DENTRO_AREA_SEGURA -> Triple(KpGreenUltralight, KpGreenDark, KpGreenAccent)
        LocationSafetyStatus.BORDA_ATENCAO -> Triple(KpYellowUltralight, KpYellowAccent, KpYellowWarning)
        LocationSafetyStatus.FORA_PERIMETRO -> Triple(KpRedLight, KpRedDark, KpRedAlert)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("location_safety_badge")
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = "${status.label} • $timeText",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AlertBanner(
    alert: UiAlert,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bg, contentColor, border) = when (alert.severity) {
        PetStatus.ALERTA -> Triple(KpRedLight, KpRedDark, KpRedAlert)
        PetStatus.ATENCAO -> Triple(KpYellowLight, KpYellowAccent, KpYellowWarning)
        PetStatus.OK -> Triple(KpGreenLight, KpGreenDark, KpGreenAccent)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .testTag("active_alert_banner")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alerta",
                tint = border,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alert.message,
                    fontSize = 12.sp,
                    color = contentColor.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("dismiss_alert_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar alerta",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PetSelectorRow(
    pets: List<Pet>,
    selectedPetId: String,
    onSelectPet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        pets.forEach { pet ->
            val isSelected = pet.id == selectedPetId
            val bg = if (isSelected) KpGreenPrimary else KpSurface
            val contentColor = if (isSelected) Color.White else KpTextPrimary
            val borderCol = if (isSelected) KpGreenPrimary else KpBorder

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                    .clickable { onSelectPet(pet.id) }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .testTag("pet_selector_${pet.id}")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White.copy(alpha = 0.2f) else KpSurfaceVariant)
                ) {
                    Text(text = pet.avatarEmoji, fontSize = 16.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pet.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = pet.currentStatus.emoji,
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = pet.species,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else KpTextMuted
                    )
                }
            }
        }
    }
}
