package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiAlert(
    val id: String,
    val petName: String,
    val title: String,
    val message: String,
    val severity: PetStatus,
    val timestamp: String
)

data class NotificationSettings(
    val heartRateAlerts: Boolean = true,
    val fenceBoundaryAlerts: Boolean = true,
    val highTemperatureAlerts: Boolean = true,
    val dailyDigestReport: Boolean = true,
    val soundAlerts: Boolean = true
)

data class AlertThresholds(
    val minBpm: Int = 50,
    val maxBpm: Int = 140,
    val maxTemperatureCelsius: Float = 39.2f,
    val defaultRadiusMeters: Float = 150f
)

data class AppUiState(
    val pets: List<Pet> = SampleData.initialPets,
    val selectedPetId: String = "thor",
    val selectedPeriod: TimePeriod = TimePeriod.HOJE,
    val showLocationHistory: Boolean = true,
    val mapCenterPetTrigger: Long = 0L,
    val activeAlert: UiAlert? = null,
    val totalReadingsToday: Int = 1428,
    val isRefreshing: Boolean = false,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            id = "init_1",
            sender = MessageSender.SAYORI,
            text = "Olá, Amanda! Sou a Sayori, sua assistente veterinária K&P. 🐾\nEstou acompanhando o Thor, a Luna e a Mel em tempo real. Como posso te ajudar hoje?",
            time = "10:40"
        )
    ),
    val isSayoriTyping: Boolean = false,
    val isRecordingAudio: Boolean = false,
    val notifications: NotificationSettings = NotificationSettings(),
    val thresholds: AlertThresholds = AlertThresholds(),
    val selectedLanguage: String = "Português (Brasil)"
) {
    val selectedPet: Pet
        get() = pets.find { it.id == selectedPetId } ?: pets.first()

    val activeAlertCount: Int
        get() = pets.count { it.currentStatus != PetStatus.OK }
}

class PetHealthViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        // Thor is initially with light temperature attention (38.2°C)
        checkActiveAlerts()
    }

    fun selectPet(petId: String) {
        _uiState.update { it.copy(selectedPetId = petId) }
    }

    fun selectPeriod(period: TimePeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    fun toggleLocationHistory() {
        _uiState.update { it.copy(showLocationHistory = !it.showLocationHistory) }
    }

    fun centerOnPet() {
        _uiState.update { it.copy(mapCenterPetTrigger = System.currentTimeMillis()) }
    }

    fun updateSafeRadius(meters: Float) {
        val currentId = _uiState.value.selectedPetId
        _uiState.update { state ->
            val updatedPets = state.pets.map { pet ->
                if (pet.id == currentId) {
                    // check if current distance falls in or out of new radius
                    val newStatus = calculateLocationStatus(pet.currentDistanceMeters, meters)
                    pet.copy(
                        safeRadiusMeters = meters,
                        locationStatus = newStatus
                    )
                } else pet
            }
            state.copy(pets = updatedPets)
        }
        checkActiveAlerts()
    }

    private fun calculateLocationStatus(distance: Float, radius: Float): LocationSafetyStatus {
        return when {
            distance > radius -> LocationSafetyStatus.FORA_PERIMETRO
            distance > radius * 0.82f -> LocationSafetyStatus.BORDA_ATENCAO
            else -> LocationSafetyStatus.DENTRO_AREA_SEGURA
        }
    }

    fun toggleSimulateFenceExit(petId: String) {
        _uiState.update { state ->
            val updatedPets = state.pets.map { pet ->
                if (pet.id == petId) {
                    if (pet.locationStatus == LocationSafetyStatus.FORA_PERIMETRO) {
                        // Return to safe zone
                        pet.copy(
                            locationStatus = LocationSafetyStatus.DENTRO_AREA_SEGURA,
                            currentStatus = if (pet.temperatureStatus == HealthStatus.ATENCAO) PetStatus.ATENCAO else PetStatus.OK,
                            statusSummary = if (pet.temperatureStatus == HealthStatus.ATENCAO) "Temperatura em 38.2°C" else "Todos os sinais normais",
                            currentDistanceMeters = 38f,
                            currentPetPosNormalizedX = 0.18f,
                            currentPetPosNormalizedY = -0.15f,
                            addressDescription = "Jardim da Residência — Rua das Acácias, 240"
                        )
                    } else {
                        // Simulate exit outside safe zone
                        pet.copy(
                            locationStatus = LocationSafetyStatus.FORA_PERIMETRO,
                            currentStatus = PetStatus.ALERTA,
                            statusSummary = "Saiu do perímetro de segurança!",
                            currentDistanceMeters = pet.safeRadiusMeters + 65f,
                            currentPetPosNormalizedX = 0.82f,
                            currentPetPosNormalizedY = 0.68f,
                            addressDescription = "Fora da Cerca — Praça Central, Rua das Flores"
                        )
                    }
                } else pet
            }
            state.copy(pets = updatedPets)
        }

        val currentPet = _uiState.value.pets.find { it.id == petId }
        if (currentPet?.locationStatus == LocationSafetyStatus.FORA_PERIMETRO) {
            triggerVibrationAlert()
            _uiState.update {
                it.copy(
                    activeAlert = UiAlert(
                        id = "alert_${System.currentTimeMillis()}",
                        petName = currentPet.name,
                        title = "🚨 ${currentPet.name} saiu da Área Segura!",
                        message = "Distância atual: ${currentPet.currentDistanceMeters.toInt()}m da base. Verifique a localização no mapa.",
                        severity = PetStatus.ALERTA,
                        timestamp = "Agora"
                    )
                )
            }
        } else {
            dismissActiveAlert()
        }
    }

    fun dismissActiveAlert() {
        _uiState.update { it.copy(activeAlert = null) }
    }

    private fun triggerVibrationAlert() {
        try {
            val context = getApplication<Application>().applicationContext
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 250), -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(500)
                }
            }
        } catch (_: Exception) {
            // gracefully fallback if vibration is not supported
        }
    }

    fun simulateRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(600)
            _uiState.update { state ->
                state.copy(
                    isRefreshing = false,
                    totalReadingsToday = state.totalReadingsToday + 3
                )
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = MessageSender.USER,
            text = text,
            time = "Agora"
        )
        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                isSayoriTyping = true
            )
        }

        viewModelScope.launch {
            delay(1000)
            val replyText = generateSayoriReply(text)
            val sayoriMsg = ChatMessage(
                id = "sayori_${System.currentTimeMillis()}",
                sender = MessageSender.SAYORI,
                text = replyText,
                time = "Agora"
            )
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + sayoriMsg,
                    isSayoriTyping = false
                )
            }
        }
    }

    fun simulateVoiceAudioRecord() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecordingAudio = true) }
            delay(1800)
            _uiState.update { it.copy(isRecordingAudio = false) }

            val voiceMsg = ChatMessage(
                id = "audio_${System.currentTimeMillis()}",
                sender = MessageSender.USER,
                text = "🎤 [Mensagem de Áudio] 'Sayori, me conte como estão os batimentos e a temperatura do Thor agora.'",
                time = "Agora",
                isAudio = true
            )

            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + voiceMsg,
                    isSayoriTyping = true
                )
            }

            delay(1200)
            val thor = _uiState.value.pets.find { it.id == "thor" } ?: _uiState.value.pets.first()
            val reply = "Ouvi sua mensagem com carinho! 💛\n\nAnalisei os dados mais recentes do Thor:\n" +
                    "💓 Batimentos: ${thor.heartRateBpm} BPM — ritmo perfeito e tranquilo.\n" +
                    "🌡️ Temperatura: ${thor.temperatureCelsius}°C — está em 38.2°C, uma ligeira elevação habitual após brincadeiras, mas dentro de margem segura.\n" +
                    "📍 Localização: Ele está na área segura do jardim.\n\nFique tranquila, continuarei vigiando cada batimento!"

            val sayoriMsg = ChatMessage(
                id = "sayori_audio_reply_${System.currentTimeMillis()}",
                sender = MessageSender.SAYORI,
                text = reply,
                time = "Agora"
            )
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + sayoriMsg,
                    isSayoriTyping = false
                )
            }
        }
    }

    private fun generateSayoriReply(query: String): String {
        val q = query.lowercase()
        val thor = _uiState.value.pets.find { it.id == "thor" }
        val luna = _uiState.value.pets.find { it.id == "luna" }
        val mel = _uiState.value.pets.find { it.id == "mel" }

        return when {
            q.contains("pets") || q.contains("ver meus pets") -> {
                "Aqui está o resumo dos seus peludos com muito carinho:\n\n" +
                        "🐕 **Thor**: 72 BPM | 98% SpO₂ | 38.2°C (Atenção suave à temp)\n" +
                        "🐱 **Luna**: 110 BPM | 99% SpO₂ | 38.4°C (Tudo excelente!)\n" +
                        "🐶 **Mel**: 92 BPM | 98% SpO₂ | 38.1°C (Perfeito estado)\n\n" +
                        "Todos estão com as coleiras K&P ativas e sincronizadas."
            }
            q.contains("alerta") || q.contains("meus alertas") -> {
                if (thor?.currentStatus == PetStatus.ATENCAO) {
                    "Temos 1 alerta preventivo ativo:\n\n" +
                            "⚠️ **Thor** está com temperatura em 38.2°C. A faixa típica para Golden Retrievers em repouso é de 37.5°C a 38.5°C. Como ele esteve brincando no jardim, isso pode ser apenas calor momentâneo. Mantenha água fresca disponível!"
                } else {
                    "Maravilha! No momento não há nenhum alerta crítico. Todos os sinais vitais e posições estão ideais. 🟢"
                }
            }
            q.contains("temperatura") || q.contains("perigoso") -> {
                "A temperatura canina e felina é naturalmente mais alta que a humana! 🌡️\n\n" +
                        "• **Normal**: 37.5°C a 39.2°C\n" +
                        "• **Atenção**: 39.3°C a 39.8°C (observe hidratação e respiração)\n" +
                        "• **Alerta**: Acima de 40.0°C requer consulta veterinária imediata.\n\n" +
                        "O K&P te avisará instantaneamente caso a curva ultrapasse o limite seguro!"
            }
            q.contains("cerca") || q.contains("geofence") || q.contains("mapa") -> {
                "A Cerca Virtual K&P utiliza GPS de alta precisão e sinal de rede inteligente. 🗺️\n\n" +
                        "• 🟢 **Zona Verde**: Área permitida onde o pet pode transitar livremente.\n" +
                        "• 🟡 **Borda Amarela**: Aproximação do limite, emitindo aviso prévio.\n" +
                        "• 🔴 **Fora do Perímetro**: Alerta sonoro imediato no seu celular.\n\n" +
                        "Você pode ajustar o raio facilmente na aba 'Mapa' usando o seletor!"
            }
            q.contains("dúvida") || q.contains("frequente") -> {
                "Aqui vão as principais dicas para a saúde do seu pet:\n" +
                        "1. Batimentos em repouso: Cães médios/grandes (60-100 BPM), Gatos (100-140 BPM).\n" +
                        "2. Oxigenação SpO₂ ideal: sempre acima de 95%.\n" +
                        "3. Bateria da coleira dura até 7 dias com transmissão inteligente."
            }
            else -> {
                "Entendi! A saúde e proteção do seu pet são nossa prioridade máxima. 🐾\n" +
                        "Os sensores da coleira K&P continuam registrando os sinais vitais a cada minuto. Posso te ajudar com detalhes sobre os batimentos, oxigenação, temperatura ou a cerca virtual?"
            }
        }
    }

    private fun checkActiveAlerts() {
        val alertPet = _uiState.value.pets.firstOrNull { it.currentStatus == PetStatus.ALERTA }
        if (alertPet != null) {
            _uiState.update {
                it.copy(
                    activeAlert = UiAlert(
                        id = "alert_${alertPet.id}",
                        petName = alertPet.name,
                        title = "Atenção necessária para ${alertPet.name}",
                        message = alertPet.statusSummary,
                        severity = alertPet.currentStatus,
                        timestamp = "Agora"
                    )
                )
            }
        }
    }

    fun updateNotificationToggle(type: String, enabled: Boolean) {
        _uiState.update { state ->
            val cur = state.notifications
            val updated = when (type) {
                "heartRate" -> cur.copy(heartRateAlerts = enabled)
                "fence" -> cur.copy(fenceBoundaryAlerts = enabled)
                "temperature" -> cur.copy(highTemperatureAlerts = enabled)
                "digest" -> cur.copy(dailyDigestReport = enabled)
                "sound" -> cur.copy(soundAlerts = enabled)
                else -> cur
            }
            state.copy(notifications = updated)
        }
    }

    fun updateThresholds(minBpm: Int, maxBpm: Int, maxTemp: Float) {
        _uiState.update {
            it.copy(
                thresholds = it.thresholds.copy(
                    minBpm = minBpm,
                    maxBpm = maxBpm,
                    maxTemperatureCelsius = maxTemp
                )
            )
        }
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(selectedLanguage = lang) }
    }
}
