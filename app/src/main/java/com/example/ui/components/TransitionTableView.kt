package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DfaModel
import com.example.ui.MissingTransitionHandling

@Composable
fun TransitionMatrixTableView(
    dfa: DfaModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DFA Transition Matrix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "→ Start   * Final",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Column {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        TableCell(
                            text = "State",
                            isHeader = true,
                            width = 110.dp
                        )
                        for (sym in dfa.alphabet) {
                            TableCell(
                                text = "Symbol '$sym'",
                                isHeader = true,
                                width = 110.dp
                            )
                        }
                    }

                    // Data Rows
                    for (state in dfa.states) {
                        val isStart = state == dfa.startState
                        val isFinal = dfa.finalStates.contains(state)
                        val prefix = buildString {
                            if (isStart) append("→ ")
                            if (isFinal) append("* ")
                        }

                        Row(
                            modifier = Modifier
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            TableCell(
                                text = "$prefix$state",
                                isHeader = false,
                                isBold = isStart || isFinal,
                                width = 110.dp,
                                color = if (isFinal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            for (sym in dfa.alphabet) {
                                val target = dfa.transitions[Pair(state, sym)]
                                TableCell(
                                    text = target ?: "∅ (no path)",
                                    isHeader = false,
                                    width = 110.dp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (target == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    isHeader: Boolean,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    color: Color = Color.Unspecified,
    fontFamily: FontFamily? = null
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (color != Color.Unspecified) color else if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontFamily = fontFamily
        )
    }
}

@Composable
fun DfaTransitionEditorView(
    dfa: DfaModel,
    missingTransitionMode: MissingTransitionHandling = MissingTransitionHandling.TREAT_AS_NO_PATH,
    onMissingTransitionModeChanged: (MissingTransitionHandling) -> Unit = {},
    onDfaChanged: (DfaModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var newStateName by remember { mutableStateOf("") }
    var newSymbolName by remember { mutableStateOf("") }

    // State for + Add Transition form
    var addFromState by remember(dfa.states) { mutableStateOf(dfa.states.firstOrNull() ?: "q0") }
    var addSymbol by remember(dfa.alphabet) { mutableStateOf(dfa.alphabet.firstOrNull() ?: "0") }
    var addToState by remember(dfa.states) { mutableStateOf(dfa.states.firstOrNull() ?: "q0") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "DFA Definition & Transitions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Add only the transitions that exist in your DFA diagram. Undefined transitions are allowed and treated as no path (∅).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Start State & Final States Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start State Dropdown
                var startDropdownExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { startDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start: ${dfa.startState}", maxLines = 1)
                    }
                    DropdownMenu(
                        expanded = startDropdownExpanded,
                        onDismissRequest = { startDropdownExpanded = false }
                    ) {
                        for (st in dfa.states) {
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    onDfaChanged(dfa.copy(startState = st))
                                    startDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Final States Chips
                Text("Final:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (st in dfa.states) {
                        val isFinal = dfa.finalStates.contains(st)
                        AssistChip(
                            onClick = {
                                val newFinals = if (isFinal) {
                                    dfa.finalStates - st
                                } else {
                                    dfa.finalStates + st
                                }
                                onDfaChanged(dfa.copy(finalStates = newFinals))
                            },
                            label = { Text(st) },
                            leadingIcon = if (isFinal) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = if (isFinal) {
                                AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                AssistChipDefaults.assistChipColors()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alphabet editor row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Alphabet Σ:", style = MaterialTheme.typography.labelMedium)
                for (sym in dfa.alphabet) {
                    AssistChip(
                        onClick = {
                            if (dfa.alphabet.size > 1) {
                                val newAlphabet = dfa.alphabet - sym
                                val newTransitions = dfa.transitions.filterKeys { it.second != sym }
                                onDfaChanged(dfa.copy(alphabet = newAlphabet, transitions = newTransitions))
                            }
                        },
                        label = { Text(sym) },
                        trailingIcon = if (dfa.alphabet.size > 1) {
                            { Icon(Icons.Default.Delete, contentDescription = "Remove symbol", modifier = Modifier.size(12.dp)) }
                        } else null
                    )
                }

                OutlinedTextField(
                    value = newSymbolName,
                    onValueChange = { newSymbolName = it.trim() },
                    placeholder = { Text("Sym") },
                    singleLine = true,
                    modifier = Modifier.width(70.dp).height(50.dp)
                )

                FilledTonalButton(
                    onClick = {
                        if (newSymbolName.isNotBlank() && !dfa.alphabet.contains(newSymbolName)) {
                            onDfaChanged(dfa.copy(alphabet = dfa.alphabet + newSymbolName))
                            newSymbolName = ""
                        }
                    },
                    enabled = newSymbolName.isNotBlank() && !dfa.alphabet.contains(newSymbolName)
                ) {
                    Text("+")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Missing Transition Handling Option
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Missing Transition Handling",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = missingTransitionMode == MissingTransitionHandling.TREAT_AS_NO_PATH,
                            onClick = { onMissingTransitionModeChanged(MissingTransitionHandling.TREAT_AS_NO_PATH) },
                            label = { Text("Treat as ∅ / No Path (Recommended)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = missingTransitionMode == MissingTransitionHandling.ADD_TRAP_STATE,
                            onClick = { onMissingTransitionModeChanged(MissingTransitionHandling.ADD_TRAP_STATE) },
                            label = { Text("Add Implicit Trap State", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Transitions may be left undefined. An undefined transition means there is no path for that symbol. The solver can internally represent this as ∅ or route it to an implicit trap state.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Existing Transitions List / Table
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Defined Transitions (${dfa.transitions.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (dfa.transitions.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "No transitions added yet. Add individual transitions below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
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
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Current State", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                        Text("Symbol", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                        Text("Next State", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                        Text("Action", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                    }

                    // Existing Transitions
                    for ((key, target) in dfa.transitions) {
                        val (from, sym) = key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(from, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("'$sym'", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text(target, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))

                            IconButton(
                                onClick = {
                                    val newTransitions = dfa.transitions.filterKeys { it != key }
                                    onDfaChanged(dfa.copy(transitions = newTransitions))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove transition",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // + Add Transition Form Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "+ Add Transition",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // From State Dropdown
                        var fromDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { fromDropdown = true },
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Text(addFromState, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = fromDropdown,
                                onDismissRequest = { fromDropdown = false }
                            ) {
                                for (st in dfa.states) {
                                    DropdownMenuItem(
                                        text = { Text(st) },
                                        onClick = {
                                            addFromState = st
                                            fromDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Symbol Dropdown
                        var symDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { symDropdown = true },
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Text("'$addSymbol'", fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = symDropdown,
                                onDismissRequest = { symDropdown = false }
                            ) {
                                for (sym in dfa.alphabet) {
                                    DropdownMenuItem(
                                        text = { Text("'$sym'") },
                                        onClick = {
                                            addSymbol = sym
                                            symDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Arrow
                        Text("→", fontWeight = FontWeight.Bold)

                        // To State Dropdown
                        var toDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { toDropdown = true },
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Text(addToState, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = toDropdown,
                                onDismissRequest = { toDropdown = false }
                            ) {
                                for (st in dfa.states) {
                                    DropdownMenuItem(
                                        text = { Text(st) },
                                        onClick = {
                                            addToState = st
                                            toDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Add Button
                        Button(
                            onClick = {
                                val newTransitions = dfa.transitions.toMutableMap()
                                newTransitions[Pair(addFromState, addSymbol)] = addToState
                                onDfaChanged(dfa.copy(transitions = newTransitions))
                            },
                            modifier = Modifier.height(40.dp).testTag("add_transition_button")
                        ) {
                            Text("+ Add", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Add/Remove State Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newStateName,
                    onValueChange = { newStateName = it.trim() },
                    placeholder = { Text("e.g. q2") },
                    label = { Text("New State") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                FilledTonalButton(
                    onClick = {
                        if (newStateName.isNotBlank() && !dfa.states.contains(newStateName)) {
                            val updatedStates = dfa.states + newStateName
                            onDfaChanged(dfa.copy(states = updatedStates))
                            newStateName = ""
                        }
                    },
                    enabled = newStateName.isNotBlank() && !dfa.states.contains(newStateName)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add State")
                }

                if (dfa.states.size > 1) {
                    OutlinedButton(
                        onClick = {
                            val lastState = dfa.states.last()
                            val updatedStates = dfa.states.dropLast(1)
                            val newStart = if (dfa.startState == lastState) updatedStates.first() else dfa.startState
                            val newFinals = (dfa.finalStates - lastState).ifEmpty { setOf(updatedStates.last()) }
                            val updatedTransitions = dfa.transitions.filterKeys {
                                it.first != lastState && dfa.transitions[it] != lastState
                            }
                            onDfaChanged(
                                dfa.copy(
                                    states = updatedStates,
                                    startState = newStart,
                                    finalStates = newFinals,
                                    transitions = updatedTransitions
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pop State")
                    }
                }
            }
        }
    }
}
