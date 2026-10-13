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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DfaPreset
import com.example.model.PresetCategory
import com.example.model.PresetsData
import com.example.ui.AppTab
import com.example.ui.MainViewModel

@Composable
fun PresetsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<PresetCategory?>(null) }
    val scrollState = rememberScrollState()

    // Random Generator Controls
    var randomStatesCount by remember { mutableIntStateOf(3) }
    var randomAlphabetSize by remember { mutableIntStateOf(2) }
    var randomFinalStatesCount by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Category Filter Chips
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All (${PresetsData.PRESETS.size})") }
                )
                FilterChip(
                    selected = selectedCategory == PresetCategory.BEGINNER,
                    onClick = { selectedCategory = PresetCategory.BEGINNER },
                    label = { Text("Beginner") }
                )
                FilterChip(
                    selected = selectedCategory == PresetCategory.INTERMEDIATE,
                    onClick = { selectedCategory = PresetCategory.INTERMEDIATE },
                    label = { Text("Intermediate") }
                )
                FilterChip(
                    selected = selectedCategory == PresetCategory.ADVANCED,
                    onClick = { selectedCategory = PresetCategory.ADVANCED },
                    label = { Text("Advanced") }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Random Generator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Casino, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Random DFA Generator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // States Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Number of States: $randomStatesCount", style = MaterialTheme.typography.bodySmall)
                        Text("(2 – 6)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Slider(
                        value = randomStatesCount.toFloat(),
                        onValueChange = { randomStatesCount = it.toInt() },
                        valueRange = 2f..6f,
                        steps = 3,
                        modifier = Modifier.testTag("random_states_slider")
                    )

                    // Alphabet Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alphabet Size: $randomAlphabetSize symbols", style = MaterialTheme.typography.bodySmall)
                        Text(if (randomAlphabetSize == 1) "Σ={0}" else if (randomAlphabetSize == 2) "Σ={0,1}" else "Σ={a,b,c}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Slider(
                        value = randomAlphabetSize.toFloat(),
                        onValueChange = { randomAlphabetSize = it.toInt() },
                        valueRange = 1f..3f,
                        steps = 1
                    )

                    // Final States Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Accepting States: $randomFinalStatesCount", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = randomFinalStatesCount.toFloat(),
                        onValueChange = { randomFinalStatesCount = it.toInt().coerceAtMost(randomStatesCount - 1) },
                        valueRange = 1f..maxOf(1f, (randomStatesCount - 1).toFloat()),
                        steps = maxOf(0, randomStatesCount - 3)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.generateRandomDfa(
                                    stateCount = randomStatesCount,
                                    alphabetSize = randomAlphabetSize,
                                    finalCount = randomFinalStatesCount,
                                    solveImmediately = false
                                )
                                viewModel.setActiveTab(AppTab.SOLVER)
                            },
                            modifier = Modifier.weight(1f).testTag("generate_random_dfa_btn")
                        ) {
                            Text("Generate Random")
                        }

                        Button(
                            onClick = {
                                viewModel.generateRandomDfa(
                                    stateCount = randomStatesCount,
                                    alphabetSize = randomAlphabetSize,
                                    finalCount = randomFinalStatesCount,
                                    solveImmediately = true
                                )
                                viewModel.setActiveTab(AppTab.SOLVER)
                            },
                            modifier = Modifier.weight(1f).testTag("generate_and_solve_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generate & Solve")
                        }
                    }
                }
            }

            Text(
                text = "Preset Automata Library",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Preset Cards
            val filteredPresets = if (selectedCategory == null) {
                PresetsData.PRESETS
            } else {
                PresetsData.PRESETS.filter { it.category == selectedCategory }
            }

            for (preset in filteredPresets) {
                PresetItemCard(
                    preset = preset,
                    onLoad = {
                        viewModel.loadPreset(preset)
                        viewModel.setWorkflowStep(0)
                        viewModel.setActiveTab(AppTab.SOLVER)
                    },
                    onSolve = {
                        viewModel.loadPreset(preset)
                        viewModel.solveDfa()
                        viewModel.setWorkflowStep(4)
                        viewModel.setActiveTab(AppTab.SOLVER)
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetItemCard(
    preset: DfaPreset,
    onLoad: () -> Unit,
    onSolve: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (preset.category) {
                        PresetCategory.BEGINNER -> MaterialTheme.colorScheme.primaryContainer
                        PresetCategory.INTERMEDIATE -> MaterialTheme.colorScheme.secondaryContainer
                        PresetCategory.ADVANCED -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                ) {
                    Text(
                        text = preset.category.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (preset.category) {
                            PresetCategory.BEGINNER -> MaterialTheme.colorScheme.onPrimaryContainer
                            PresetCategory.INTERMEDIATE -> MaterialTheme.colorScheme.onSecondaryContainer
                            PresetCategory.ADVANCED -> MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "States: ${preset.dfa.states.size} | Alphabet: {${preset.dfa.alphabet.joinToString()}}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLoad,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Load & Edit")
                }

                Button(
                    onClick = onSolve,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Solve")
                }
            }
        }
    }
}
