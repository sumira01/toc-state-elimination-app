package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.SolverScreen
import com.example.ui.screens.StepByStepScreen
import com.example.ui.screens.VisualizerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Automaton?") },
            text = { Text("This will reset all states, alphabet, transitions, and generated steps back to the default example.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAll()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "State Elimination Tool",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "DFA → GNFA → Regular Expression",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Light/Dark toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (uiState.isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Reset button
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("reset_all_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Automaton",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.SOLVER,
                    onClick = { viewModel.setActiveTab(AppTab.SOLVER) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Solver") },
                    label = { Text("Solver", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_solver")
                )
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.VISUALIZER,
                    onClick = { viewModel.setActiveTab(AppTab.VISUALIZER) },
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = "Visualizer") },
                    label = { Text("Visualizer", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_visualizer")
                )
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.STEP_BY_STEP,
                    onClick = { viewModel.setActiveTab(AppTab.STEP_BY_STEP) },
                    icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = "Step-by-Step") },
                    label = { Text("Steps", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_steps")
                )
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.PRESETS,
                    onClick = { viewModel.setActiveTab(AppTab.PRESETS) },
                    icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Presets") },
                    label = { Text("Presets", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_presets")
                )
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.ABOUT,
                    onClick = { viewModel.setActiveTab(AppTab.ABOUT) },
                    icon = { Icon(Icons.Default.School, contentDescription = "Concepts") },
                    label = { Text("Concepts", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_about")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                AppTab.SOLVER -> SolverScreen(viewModel)
                AppTab.VISUALIZER -> VisualizerScreen(viewModel)
                AppTab.STEP_BY_STEP -> StepByStepScreen(viewModel)
                AppTab.PRESETS -> PresetsScreen(viewModel)
                AppTab.ABOUT -> AboutScreen(viewModel)
            }
        }
    }
}
