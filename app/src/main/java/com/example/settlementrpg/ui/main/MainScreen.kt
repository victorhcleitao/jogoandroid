package com.example.settlementrpg.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.settlementrpg.ui.viewmodel.GameViewModel
import com.example.settlementrpg.ui.screens.MapScreen
import com.example.settlementrpg.ui.screens.GuildScreen
import com.example.settlementrpg.ui.screens.LogScreen
import com.example.settlementrpg.theme.*

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Guildkeeper",
                    color = GoldPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Recursos Rápidos Globais
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "🪙 ${gameState.gold}", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "★ ${gameState.reputation}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Mapa", tint = if (selectedTab == 0) GoldPrimary else TextGray) },
                    label = { Text("Mapa", color = if (selectedTab == 0) GoldPrimary else TextGray) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = DarkSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Guilda", tint = if (selectedTab == 1) GoldPrimary else TextGray) },
                    label = { Text("Guilda", color = if (selectedTab == 1) GoldPrimary else TextGray) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = DarkSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Eventos", tint = if (selectedTab == 2) GoldPrimary else TextGray) },
                    label = { Text("Eventos", color = if (selectedTab == 2) GoldPrimary else TextGray) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = DarkSurfaceVariant
                    )
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (selectedTab) {
                0 -> MapScreen(gameState = gameState, modifier = Modifier.fillMaxSize())
                1 -> GuildScreen(
                    gameState = gameState,
                    onUpgradeBuilding = { viewModel.upgradeBuilding(it) },
                    onPublishMission = { viewModel.publishMission(it) },
                    onSellMaterial = { viewModel.sellMaterialFromGuild(it) },
                    onCraftEquipment = { viewModel.craftEquipment(it) },
                    onEquipHero = { heroId, equipId -> viewModel.equipHero(heroId, equipId) },
                    modifier = Modifier.fillMaxSize()
                )
                2 -> LogScreen(logs = gameState.logs, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
