package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GnfaModel
import com.example.model.RegexNode
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.ViewMode
import com.example.ui.components.AutomataGraphView
import com.example.ui.components.DfaTransitionEditorView
import com.example.ui.components.StateEliminationFormulaCard
import com.example.ui.components.StepProgressIndicator
import com.example.ui.components.TransitionMatrixTableView
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolverScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top 5-Step Progress Bar
        StepProgressIndicator(
            currentStepIndex = uiState.currentWorkflowStep,
            onStepSelected = { viewModel.setWorkflowStep(it) }
        )

        // Sub-header toolbar: Mode toggles and quick action
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Learning Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = uiState.isLearningModeEnabled,
                        onCheckedChange = { viewModel.toggleLearningMode() },
                        modifier = Modifier.testTag("learning_mode_switch")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.solveDfa()
                            viewModel.setWorkflowStep(4)
                        },
                        modifier = Modifier.testTag("auto_solve_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto Solve", fontSize = 12.sp)
                    }
                }
            }
        }

        // Scrollable Step Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Validation Warning Alert (if any)
            if (!uiState.validationResult.isValid) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DFA Validation Issues",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        for (err in uiState.validationResult.errors) {
                            Text(
                                text = "• $err",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            } else if (uiState.validationResult.infoMessages.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        for (info in uiState.validationResult.infoMessages) {
                            Text(
                                text = "ℹ️ $info",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Learning Mode banner
            if (uiState.isLearningModeEnabled) {
                LearningBanner(stepIndex = uiState.currentWorkflowStep)
            }

            // Render current step view
            when (uiState.currentWorkflowStep) {
                0 -> Step1DefineDfa(viewModel)
                1 -> Step2VisualizeDfa(viewModel)
                2 -> Step3ConvertToGnfa(viewModel)
                3 -> Step4EliminateStates(viewModel)
                4 -> Step5RegexResult(viewModel)
            }
        }
    }
}

@Composable
private fun LearningBanner(stepIndex: Int) {
    val (title, text) = when (stepIndex) {
        0 -> Pair(
            "Step 1: Formal Definition",
            "A DFA consists of 5 tuples: States (Q), Alphabet (Σ), Transition function (δ), Start state (q0), and Final states (F). Add the transitions that exist in your DFA. Undefined transitions are allowed and are treated as no available path (∅)."
        )
        1 -> Pair(
            "Step 2: Inspecting the Topology",
            "Observe loops, cycles, and accepting states. The State Elimination method works by systematically eliminating internal nodes while replacing direct edges with regular expressions that preserve all paths."
        )
        2 -> Pair(
            "Step 3: Generalized NFA (GNFA)",
            "We construct a GNFA by introducing a single start state q_start (with an ε transition to original start) and a single accept state q_accept (with ε transitions from all original accept states). Now edge labels are regular expressions!"
        )
        3 -> Pair(
            "Step 4: State Elimination Formula",
            "For state q_k to be removed: any path i → q_k → j is bypassed by updating R_ij = R_ij | R_ik·(R_kk)*·R_kj. Notice that loops on q_k become Kleene Star expressions (*)."
        )
        else -> Pair(
            "Step 5: Final Regular Expression",
            "All intermediate states have been eliminated! The single remaining transition between q_start and q_accept represents the complete regular language accepted by the original DFA."
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// STEP 1: DEFINE DFA
@Composable
private fun Step1DefineDfa(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DfaTransitionEditorView(
            dfa = uiState.dfa,
            missingTransitionMode = uiState.missingTransitionMode,
            onMissingTransitionModeChanged = { viewModel.setMissingTransitionMode(it) },
            onDfaChanged = { viewModel.setDfa(it) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.setActiveTab(AppTab.PRESETS) }
            ) {
                Text("Browse Presets")
            }

            Button(
                onClick = { viewModel.setWorkflowStep(1) },
                enabled = uiState.validationResult.isValid
            ) {
                Text("Continue to Visualize")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// STEP 2: VISUALIZE DFA
@Composable
private fun Step2VisualizeDfa(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val displayDfa = uiState.effectiveDfa

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DFA Topology",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Switch between Graph View & Table View
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = uiState.currentViewMode == ViewMode.GRAPH,
                    onClick = { viewModel.setViewMode(ViewMode.GRAPH) },
                    label = { Text("Graph View") }
                )
                FilterChip(
                    selected = uiState.currentViewMode == ViewMode.TABLE,
                    onClick = { viewModel.setViewMode(ViewMode.TABLE) },
                    label = { Text("Table View") }
                )
            }
        }

        if (uiState.currentViewMode == ViewMode.GRAPH) {
            AutomataGraphView(
                dfa = displayDfa,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            )
        } else {
            TransitionMatrixTableView(dfa = displayDfa)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { viewModel.setWorkflowStep(0) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit DFA")
            }

            Button(onClick = { viewModel.setWorkflowStep(2) }) {
                Text("Convert to GNFA")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// STEP 3: CONVERT TO GNFA
@Composable
private fun Step3ConvertToGnfa(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val initialGnfa = remember(uiState.effectiveDfa) { GnfaModel.fromDfa(uiState.effectiveDfa) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Conversion to GNFA Completed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A Generalized NFA (GNFA) is constructed to support state elimination:",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. Added new start state 'q_start' with ε-transition to original start '${uiState.dfa.startState}'.\n" +
                            "2. Added new accept state 'q_accept' with ε-transitions from original final states (${uiState.dfa.finalStates.joinToString()}).\n" +
                            "3. All transitions are now labeled with Regular Expressions (e.g. multi-symbol transitions merged with '|').",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        // GNFA Graph
        AutomataGraphView(
            gnfa = initialGnfa,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { viewModel.setWorkflowStep(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back")
            }

            Button(onClick = { viewModel.setWorkflowStep(3) }) {
                Text("Begin State Elimination")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// STEP 4: ELIMINATE STATES
@Composable
private fun Step4EliminateStates(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val solution = uiState.solution

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StateEliminationFormulaCard()

        // Mode switch: Auto solve steps vs Manual elimination
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Elimination Mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = !uiState.isManualMode,
                            onClick = { viewModel.setManualMode(false) },
                            label = { Text("Auto Guided") }
                        )
                        FilterChip(
                            selected = uiState.isManualMode,
                            onClick = { viewModel.setManualMode(true) },
                            label = { Text("Manual Pick") }
                        )
                    }
                }

                if (uiState.isManualMode) {
                    // Manual Mode
                    val currentGnfa = uiState.currentGnfa ?: GnfaModel.fromDfa(uiState.dfa)
                    val eliminable = currentGnfa.eliminableStates()

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select a state to eliminate:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (eliminable.isEmpty()) {
                        Text(
                            text = "✓ All intermediate states have been eliminated!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    } else {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (st in eliminable) {
                                Button(
                                    onClick = { viewModel.eliminateManualState(st) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Text("Eliminate $st")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    AutomataGraphView(
                        gnfa = currentGnfa,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    )
                } else if (solution != null && solution.steps.isNotEmpty()) {
                    // Auto Guided Steps
                    val activeIndex = uiState.activeStepIndex.coerceIn(0, solution.steps.size - 1)
                    val step = solution.steps[activeIndex]

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step Navigator Carousel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.setActiveStepIndex(activeIndex - 1) },
                            enabled = activeIndex > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step")
                        }

                        Text(
                            text = "Step ${step.stepNumber} of ${solution.steps.size}: Eliminate '${step.eliminatedState}'",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = { viewModel.setActiveStepIndex(activeIndex + 1) },
                            enabled = activeIndex < solution.steps.size - 1
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Step")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = step.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Graph showing updated GNFA with highlighted eliminated state
                    AutomataGraphView(
                        gnfa = step.gnfaBefore,
                        eliminatingState = step.eliminatedState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Path Updates Table
                    Text(
                        text = "Updated Path Expressions:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        for (up in step.updates.take(6)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${up.fromState} → ${up.toState}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = up.newTransition.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { viewModel.setWorkflowStep(2) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("GNFA")
            }

            Button(onClick = { viewModel.setWorkflowStep(4) }) {
                Text("View Final Regex")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// STEP 5: REGEX RESULT & VERIFICATION
@Composable
private fun Step5RegexResult(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val solution = uiState.solution
    val regex = solution?.finalRegex ?: RegexNode.EmptySet
    val regexString = regex.toString()
    val verification = uiState.verificationSummary

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Large Final Regex Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                MaterialTheme.colorScheme.primary
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EQUIVALENT REGULAR EXPRESSION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Equivalent to original DFA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big expression display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = regexString,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Copy, Share, etc.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.copyToClipboard(regexString, "Regular Expression") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Regex")
                    }

                    OutlinedButton(
                        onClick = {
                            val export = viewModel.exportSolutionText()
                            viewModel.copyToClipboard(export, "Complete Solution")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Solution")
                    }
                }
            }
        }

        // Understanding the Result explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Understanding the Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "This expression denotes the regular language L(M) of all finite strings over alphabet Σ={${uiState.dfa.alphabet.joinToString()}} accepted by the automaton. " +
                            "Operators follow standard precedence: Kleene Star (*) has highest precedence, followed by Concatenation (·), followed by Union (|).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        // Empirical Verification Table
        if (verification != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Empirical Verification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (verification.allPassed) SuccessGreen.copy(alpha = 0.15f) else WarningOrange.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (verification.allPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (verification.allPassed) SuccessGreen else WarningOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${verification.passedCount}/${verification.totalTested} Passed",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (verification.allPassed) SuccessGreen else WarningOrange
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = verification.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Verification Results Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("Test String", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1.2f))
                            Text("DFA", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                            Text("Regex", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                            Text("Match", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.8f))
                        }

                        // Rows (first 10 strings)
                        for (item in verification.results.take(12)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.displayInput,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = if (item.dfaAccepted) "Accepted" else "Rejected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.dfaAccepted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (item.regexAccepted) "Accepted" else "Rejected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.regexAccepted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (item.isMatch) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (item.isMatch) SuccessGreen else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp).weight(0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Restart button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(onClick = { viewModel.setWorkflowStep(0) }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Again with New DFA")
            }
        }
    }
}
