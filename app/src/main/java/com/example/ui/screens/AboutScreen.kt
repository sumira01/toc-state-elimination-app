package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.PresetsData
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.StateEliminationFormulaCard

data class ConceptItem(
    val title: String,
    val summary: String,
    val details: String,
    val formula: String? = null
)

val CONCEPTS = listOf(
    ConceptItem(
        title = "DFA (Deterministic Finite Automaton)",
        summary = "A 5-tuple (Q, Σ, δ, q0, F) where every state has exactly one next state per symbol.",
        details = "In a DFA, there are no ambiguous choices or ε (empty) transitions. For each input string, there is exactly one deterministic path from the start state to either an accepting or rejecting state."
    ),
    ConceptItem(
        title = "GNFA (Generalized NFA)",
        summary = "An NFA where transition arrows are labeled with arbitrary Regular Expressions.",
        details = "A GNFA has exactly one start state with only outgoing arrows, and exactly one accept state with only incoming arrows. It serves as the foundation for the State Elimination method."
    ),
    ConceptItem(
        title = "State Elimination Method",
        summary = "An algorithm that converts any finite automaton into an equivalent Regular Expression.",
        details = "States are eliminated one by one. Whenever state qk is eliminated, any incoming path from qi and outgoing path to qj is replaced by a direct regular expression: Rij = Rij | Rik(Rkk)*Rkj.",
        formula = "R_ij = R_ij | R_ik · (R_kk)* · R_kj"
    ),
    ConceptItem(
        title = "Kleene Star (*)",
        summary = "Represents repetition: zero or more occurrences of an expression.",
        details = "For language L, L* contains ε, L, LL, LLL, etc. If state qk has a self-loop with label R, looping on qk corresponds to the regular expression R*."
    ),
    ConceptItem(
        title = "Empty String (ε)",
        summary = "A string of length 0. Not the same as the empty set ∅.",
        details = "ε represents taking a step without consuming any input symbols. In regular expression identities: R · ε = R and ε* = ε."
    ),
    ConceptItem(
        title = "Empty Set (∅)",
        summary = "A language containing no strings at all.",
        details = "An edge labeled with ∅ means there is no path between the two states. Identities: R | ∅ = R, R · ∅ = ∅, and ∅* = ε."
    ),
    ConceptItem(
        title = "Union ( | ) & Concatenation",
        summary = "Union represents alternatives; Concatenation represents sequential ordering.",
        details = "R1 | R2 accepts strings matching R1 OR R2. R1 · R2 accepts strings formed by an R1 string followed by an R2 string."
    )
)

@Composable
fun AboutScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "State Elimination Tool",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "DFA → GNFA → Regular Expression",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Convert finite automata into regular expressions using the State Elimination Method — step by step with interactive graph visualization and empirical verification.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val preset = PresetsData.PRESETS.first()
                        viewModel.loadPreset(preset)
                        viewModel.solveDfa()
                        viewModel.setWorkflowStep(4)
                        viewModel.setActiveTab(AppTab.SOLVER)
                    },
                    modifier = Modifier.testTag("see_example_solution_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("See Example Solution")
                }
            }
        }

        // Dedicated Formula Card
        StateEliminationFormulaCard()

        // How to Use Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How to Use This Tool",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                val steps = listOf(
                    "1. Define or Load a DFA: Enter states, alphabet, start state, accepting states, and transitions, or pick a preset.",
                    "2. Inspect the Topology: View the interactive directed graph or transition matrix.",
                    "3. Convert to GNFA: System adds distinct q_start and q_accept nodes and merges multi-symbol edges.",
                    "4. Choose Elimination Mode: Use Auto Guided mode or pick states manually to observe path updates.",
                    "5. Review Mathematical Derivations: See the formula substituted with real values for each path.",
                    "6. Obtain the Final Regular Expression: Copy regex, export the solution, or examine component meanings.",
                    "7. Verify Empirically: Check generated test strings to confirm DFA and Regex equivalence."
                )

                for (s in steps) {
                    Text(
                        text = s,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }

        // Quick Concepts Accordion
        Text(
            text = "Quick Concepts & Theory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        for (concept in CONCEPTS) {
            ConceptExpandableCard(concept = concept)
        }

        // Viva / Exam Tips Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Viva & Exam Preparation Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Q: Why do we add new q_start and q_accept states in a GNFA?\n" +
                            "A: In a GNFA, the start state must have NO incoming transitions and the accept state must have NO outgoing transitions. In an arbitrary DFA, the start state could have self-loops or incoming arrows, and accept states could have outgoing transitions. Adding unique q_start and q_accept guarantees the invariant.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Q: Does the order of state elimination matter?\n" +
                            "A: The final regular expressions obtained via different elimination orders may look syntactically different, but they are all semantically equivalent (they represent the exact same regular language).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ConceptExpandableCard(concept: ConceptItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = concept.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = concept.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = concept.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    if (concept.formula != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = concept.formula,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
