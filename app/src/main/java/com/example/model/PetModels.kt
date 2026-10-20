package com.example.model

enum class PetStatus(val label: String, val emoji: String) {
    OK("Normal", "🟢"),
    ATENCAO("Atenção", "🟡"),
    ALERTA("Alerta", "🔴")
}

enum class HealthStatus(val label: String, val emoji: String) {
    NORMAL("Normal", "🟢"),
    ATENCAO("Atenção", "🟡"),
    ALERTA("Alerta", "🔴")
}

enum class LocationSafetyStatus(val label: String, val emoji: String) {
    DENTRO_AREA_SEGURA("Dentro da área segura", "🟢"),
    BORDA_ATENCAO("Na borda da cerca", "🟡"),
    FORA_PERIMETRO("Fora do perímetro", "🔴")
}

enum class TimePeriod(val title: String) {
    HOJE("Hoje"),
    SETE_DIAS("7 dias"),
    TRINTA_DIAS("30 dias")
}

data class VitalSample(
    val timeLabel: String,
    val heartRate: Int,
    val spo2: Int,
    val temperature: Float
)

data class LocationBreadcrumb(
    val id: String,
    val time: String,
    val latOffset: Float, // relative to center for canvas
    val lngOffset: Float,
    val address: String,
    val status: LocationSafetyStatus
)

data class Pet(
    val id: String,
    val name: String,
    val species: String,
    val breed: String,
    val age: String,
    val weight: String,
    val collarId: String,
    val batteryPercent: Int,
    val signalStrength: String,
    val avatarEmoji: String,
    val currentStatus: PetStatus,
    val statusSummary: String,
    val heartRateBpm: Int,
    val heartRateStatus: HealthStatus,
    val spo2Percent: Int,
    val spo2Status: HealthStatus,
    val temperatureCelsius: Float,
    val temperatureStatus: HealthStatus,
    val locationStatus: LocationSafetyStatus,
    val lastUpdate: String,
    val safeRadiusMeters: Float,
    val currentDistanceMeters: Float,
    val currentPetPosNormalizedX: Float, // -1f to 1f relative to center
    val currentPetPosNormalizedY: Float,
    val addressDescription: String,
    val breadcrumbs: List<LocationBreadcrumb>,
    val todaySamples: List<VitalSample>,
    val weekSamples: List<VitalSample>,
    val monthSamples: List<VitalSample>
)

enum class MessageSender {
    USER,
    SAYORI
}

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val time: String,
    val isAudio: Boolean = false
)
