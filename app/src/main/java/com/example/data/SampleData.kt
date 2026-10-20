package com.example.data

import com.example.model.*

object SampleData {

    val thorTodaySamples = listOf(
        VitalSample("06:00", 68, 98, 37.9f),
        VitalSample("07:30", 70, 98, 38.0f),
        VitalSample("09:00", 75, 99, 38.1f),
        VitalSample("10:00", 72, 98, 38.2f),
        VitalSample("10:15", 74, 97, 38.2f),
        VitalSample("10:30", 71, 98, 38.2f),
        VitalSample("10:45", 72, 98, 38.2f)
    )

    val thorWeekSamples = listOf(
        VitalSample("Seg", 70, 98, 38.0f),
        VitalSample("Ter", 74, 97, 38.1f),
        VitalSample("Qua", 71, 98, 37.9f),
        VitalSample("Qui", 76, 98, 38.3f),
        VitalSample("Sex", 73, 99, 38.1f),
        VitalSample("Sáb", 78, 98, 38.4f),
        VitalSample("Hoje", 72, 98, 38.2f)
    )

    val thorMonthSamples = listOf(
        VitalSample("Sem 1", 72, 98, 38.1f),
        VitalSample("Sem 2", 75, 97, 38.0f),
        VitalSample("Sem 3", 71, 98, 38.2f),
        VitalSample("Sem 4", 72, 98, 38.2f)
    )

    val lunaTodaySamples = listOf(
        VitalSample("06:00", 102, 99, 38.2f),
        VitalSample("08:00", 108, 98, 38.3f),
        VitalSample("10:00", 114, 99, 38.4f),
        VitalSample("10:52", 110, 99, 38.4f)
    )

    val melTodaySamples = listOf(
        VitalSample("06:00", 88, 98, 38.0f),
        VitalSample("08:30", 94, 98, 38.1f),
        VitalSample("10:00", 90, 99, 38.0f),
        VitalSample("10:48", 92, 98, 38.1f)
    )

    val initialPets = listOf(
        Pet(
            id = "thor",
            name = "Thor",
            species = "Cachorro",
            breed = "Golden Retriever",
            age = "3 anos",
            weight = "28.5 kg",
            collarId = "KP-COLLAR-8921",
            batteryPercent = 88,
            signalStrength = "Forte (4G / LoRa)",
            avatarEmoji = "🐕",
            currentStatus = PetStatus.ATENCAO,
            statusSummary = "Temperatura levemente elevada",
            heartRateBpm = 72,
            heartRateStatus = HealthStatus.NORMAL,
            spo2Percent = 98,
            spo2Status = HealthStatus.NORMAL,
            temperatureCelsius = 38.2f,
            temperatureStatus = HealthStatus.ATENCAO,
            locationStatus = LocationSafetyStatus.DENTRO_AREA_SEGURA,
            lastUpdate = "10:45",
            safeRadiusMeters = 150f,
            currentDistanceMeters = 38f,
            currentPetPosNormalizedX = 0.18f,
            currentPetPosNormalizedY = -0.15f,
            addressDescription = "Jardim da Residência — Rua das Acácias, 240",
            breadcrumbs = listOf(
                LocationBreadcrumb("b1", "08:15", 0.00f, 0.00f, "Casa (Ponto Base)", LocationSafetyStatus.DENTRO_AREA_SEGURA),
                LocationBreadcrumb("b2", "09:30", 0.12f, 0.10f, "Pátio Interno", LocationSafetyStatus.DENTRO_AREA_SEGURA),
                LocationBreadcrumb("b3", "10:15", 0.25f, -0.08f, "Jardim Frontal", LocationSafetyStatus.DENTRO_AREA_SEGURA),
                LocationBreadcrumb("b4", "10:45", 0.18f, -0.15f, "Sob a Sombra da Árvore", LocationSafetyStatus.DENTRO_AREA_SEGURA)
            ),
            todaySamples = thorTodaySamples,
            weekSamples = thorWeekSamples,
            monthSamples = thorMonthSamples
        ),
        Pet(
            id = "luna",
            name = "Luna",
            species = "Gato",
            breed = "Siamês",
            age = "2 anos",
            weight = "4.2 kg",
            collarId = "KP-COLLAR-3341",
            batteryPercent = 94,
            signalStrength = "Forte (4G / LoRa)",
            avatarEmoji = "🐱",
            currentStatus = PetStatus.OK,
            statusSummary = "Todos os sinais normais",
            heartRateBpm = 110,
            heartRateStatus = HealthStatus.NORMAL,
            spo2Percent = 99,
            spo2Status = HealthStatus.NORMAL,
            temperatureCelsius = 38.4f,
            temperatureStatus = HealthStatus.NORMAL,
            locationStatus = LocationSafetyStatus.DENTRO_AREA_SEGURA,
            lastUpdate = "10:52",
            safeRadiusMeters = 80f,
            currentDistanceMeters = 14f,
            currentPetPosNormalizedX = -0.08f,
            currentPetPosNormalizedY = 0.10f,
            addressDescription = "Sala de Estar — Sofá Principal",
            breadcrumbs = listOf(
                LocationBreadcrumb("bl1", "07:00", 0.00f, 0.00f, "Cama do Pet", LocationSafetyStatus.DENTRO_AREA_SEGURA),
                LocationBreadcrumb("bl2", "08:45", -0.05f, 0.06f, "Varanda Fechada", LocationSafetyStatus.DENTRO_AREA_SEGURA),
                LocationBreadcrumb("bl3", "10:52", -0.08f, 0.10f, "Sala de Estar", LocationSafetyStatus.DENTRO_AREA_SEGURA)
            ),
            todaySamples = lunaTodaySamples,
            weekSamples = thorWeekSamples,
            monthSamples = thorMonthSamples
        ),
        Pet(
            id = "mel",
            name = "Mel",
            species = "Cachorro",
            breed = "Spitz Alemão",
            age = "4 anos",
            weight = "5.8 kg",
            collarId = "KP-COLLAR-7712",
            batteryPercent = 64,
            signalStrength = "Médio (LoRa)",
            avatarEmoji = "🐶",
            currentStatus = PetStatus.OK,
            statusSummary = "Todos os sinais normais",
            heartRateBpm = 92,
            heartRateStatus = HealthStatus.NORMAL,
            spo2Percent = 98,
            spo2Status = HealthStatus.NORMAL,
            temperatureCelsius = 38.1f,
            temperatureStatus = HealthStatus.NORMAL,
            locationStatus = LocationSafetyStatus.DENTRO_AREA_SEGURA,
            lastUpdate = "10:48",
            safeRadiusMeters = 120f,
            currentDistanceMeters = 25f,
            currentPetPosNormalizedX = 0.06f,
            currentPetPosNormalizedY = 0.14f,
            addressDescription = "Deck da Piscina — Área Coberta",
            breadcrumbs = listOf(
                LocationBreadcrumb("bm1", "08:00", 0.00f, 0.00f, "Casa (Quarto)", LocationSafetyStatus.DENTRO_AREA_SEGURA),
                LocationBreadcrumb("bm2", "09:15", 0.04f, 0.08f, "Cozinha", LocationSafetyStatus.DENTRO_AREA_SEGURA),
                LocationBreadcrumb("bm3", "10:48", 0.06f, 0.14f, "Deck da Piscina", LocationSafetyStatus.DENTRO_AREA_SEGURA)
            ),
            todaySamples = melTodaySamples,
            weekSamples = thorWeekSamples,
            monthSamples = thorMonthSamples
        )
    )
}
