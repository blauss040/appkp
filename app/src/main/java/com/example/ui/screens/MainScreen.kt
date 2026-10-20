package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PetStatus
import com.example.ui.theme.KpGreenDark
import com.example.ui.theme.KpGreenPrimary
import com.example.ui.theme.KpRedAlert
import com.example.viewmodel.AppUiState
import com.example.viewmodel.PetHealthViewModel

enum class NavigationScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Início", Icons.Filled.Home, Icons.Outlined.Home),
    CHARTS("Gráficos", Icons.Filled.ShowChart, Icons.Outlined.ShowChart),
    MAP("Mapa", Icons.Filled.LocationOn, Icons.Outlined.LocationOn),
    SAYORI("Sayori", Icons.Filled.Pets, Icons.Outlined.Pets),
    PROFILE("Perfil", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainScreen(
    viewModel: PetHealthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(NavigationScreen.HOME) }

    // Interceptar botão Voltar do sistema para retornar à tela Inicial
    BackHandler(enabled = currentScreen != NavigationScreen.HOME) {
        currentScreen = NavigationScreen.HOME
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                NavigationScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    val hasMapBadge = screen == NavigationScreen.MAP && uiState.pets.any { it.currentStatus != PetStatus.OK }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (hasMapBadge) {
                                        Badge(
                                            containerColor = KpRedAlert,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = "!", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KpGreenDark,
                            selectedTextColor = KpGreenPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            label = "screen_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { screen ->
            when (screen) {
                NavigationScreen.HOME -> HomeScreen(
                    uiState = uiState,
                    onSelectPet = { petId -> viewModel.selectPet(petId) },
                    onNavigateToCharts = { petId ->
                        viewModel.selectPet(petId)
                        currentScreen = NavigationScreen.CHARTS
                    },
                    onNavigateToMap = { petId ->
                        viewModel.selectPet(petId)
                        currentScreen = NavigationScreen.MAP
                    },
                    onNavigateToSayori = { currentScreen = NavigationScreen.SAYORI },
                    onDismissAlert = { viewModel.dismissActiveAlert() },
                    onRefresh = { viewModel.simulateRefresh() }
                )

                NavigationScreen.CHARTS -> ChartsScreen(
                    uiState = uiState,
                    onSelectPet = { petId -> viewModel.selectPet(petId) },
                    onSelectPeriod = { period -> viewModel.selectPeriod(period) }
                )

                NavigationScreen.MAP -> MapScreen(
                    uiState = uiState,
                    onSelectPet = { petId -> viewModel.selectPet(petId) },
                    onUpdateSafeRadius = { radius -> viewModel.updateSafeRadius(radius) },
                    onToggleSimulateExit = { petId -> viewModel.toggleSimulateFenceExit(petId) },
                    onToggleHistory = { viewModel.toggleLocationHistory() },
                    onCenterPet = { viewModel.centerOnPet() }
                )

                NavigationScreen.SAYORI -> SayoriScreen(
                    uiState = uiState,
                    onSendMessage = { text -> viewModel.sendMessage(text) },
                    onSimulateVoice = { viewModel.simulateVoiceAudioRecord() }
                )

                NavigationScreen.PROFILE -> ProfileScreen(
                    uiState = uiState,
                    onUpdateNotification = { key, enabled -> viewModel.updateNotificationToggle(key, enabled) },
                    onUpdateThresholds = { minBpm, maxBpm, maxTemp -> viewModel.updateThresholds(minBpm, maxBpm, maxTemp) },
                    onSelectLanguage = { lang -> viewModel.setLanguage(lang) },
                    onSelectPet = { petId -> viewModel.selectPet(petId) }
                )
            }
        }
    }
}
