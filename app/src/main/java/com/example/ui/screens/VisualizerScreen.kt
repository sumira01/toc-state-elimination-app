package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GnfaModel
import com.example.ui.MainViewModel
import com.example.ui.components.AutomataGraphView
import com.example.ui.components.TransitionMatrixTableView
import com.example.ui.theme.GnfaAcceptColor
import com.example.ui.theme.GnfaStartColor

enum class VisualizerTab {
    DFA_GRAPH,
    DFA_TABLE,
    GNFA_GRAPH
}

@Composable
fun VisualizerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(VisualizerTab.DFA_GRAPH) }

    val currentDfa = uiState.effectiveDfa
    val currentGnfa = remember(currentDfa) { GnfaModel.fromDfa(currentDfa) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == VisualizerTab.DFA_GRAPH,
                onClick = { selectedTab = VisualizerTab.DFA_GRAPH },
                label = { Text("DFA Graph") }
            )
            FilterChip(
                selected = selectedTab == VisualizerTab.DFA_TABLE,
                onClick = { selectedTab = VisualizerTab.DFA_TABLE },
                label = { Text("Transition Table") }
            )
            FilterChip(
                selected = selectedTab == VisualizerTab.GNFA_GRAPH,
                onClick = { selectedTab = VisualizerTab.GNFA_GRAPH },
                label = { Text("GNFA Graph") }
            )
        }

        // Main Viewer Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                VisualizerTab.DFA_GRAPH -> {
                    AutomataGraphView(
                        dfa = currentDfa,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    )
                }
                VisualizerTab.DFA_TABLE -> {
                    TransitionMatrixTableView(
                        dfa = currentDfa,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                VisualizerTab.GNFA_GRAPH -> {
                    AutomataGraphView(
                        gnfa = currentGnfa,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    )
                }
            }
        }

        // Legend Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    isDoubleCircle = false,
                    label = "Start State (→)"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    isDoubleCircle = true,
                    label = "Accepting (*)"
                )
                if (selectedTab == VisualizerTab.GNFA_GRAPH) {
                    LegendItem(
                        color = GnfaStartColor,
                        isDoubleCircle = false,
                        label = "q_start"
                    )
                    LegendItem(
                        color = GnfaAcceptColor,
                        isDoubleCircle = true,
                        label = "q_accept"
                    )
                } else {
                    LegendItem(
                        color = MaterialTheme.colorScheme.outline,
                        isDoubleCircle = false,
                        label = "Normal State"
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    isDoubleCircle: Boolean,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color.copy(alpha = 0.2f), CircleShape)
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDoubleCircle) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .border(1.5.dp, color, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
