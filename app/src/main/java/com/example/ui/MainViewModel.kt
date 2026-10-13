package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.model.DfaModel
import com.example.model.DfaPreset
import com.example.model.GnfaModel
import com.example.model.PresetsData
import com.example.model.RegexNode
import com.example.model.SolutionResult
import com.example.model.StateEliminationEngine
import com.example.model.ValidationResult
import com.example.model.VerificationEngine
import com.example.model.VerificationSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MissingTransitionHandling {
    TREAT_AS_NO_PATH,
    ADD_TRAP_STATE
}

data class AppUiState(
    val dfa: DfaModel = PresetsData.PRESETS.first().dfa,
    val validationResult: ValidationResult = PresetsData.PRESETS.first().dfa.validate(),
    val missingTransitionMode: MissingTransitionHandling = MissingTransitionHandling.TREAT_AS_NO_PATH,
    val currentWorkflowStep: Int = 0, // 0: Define, 1: Visualize, 2: GNFA, 3: Elimination, 4: Regex Result
    val currentGnfa: GnfaModel? = null,
    val solution: SolutionResult? = null,
    val activeStepIndex: Int = 0, // For inspecting step-by-step
    val manualGnfaHistory: List<Pair<GnfaModel, String>> = emptyList(), // For manual step-by-step elimination
    val isManualMode: Boolean = false,
    val customEliminationOrder: List<String> = emptyList(),
    val verificationSummary: VerificationSummary? = null,
    val isLearningModeEnabled: Boolean = true,
    val isDarkMode: Boolean = false, // Default is Light Mode per requirements
    val currentViewMode: ViewMode = ViewMode.GRAPH, // GRAPH or TABLE
    val activeTab: AppTab = AppTab.SOLVER
) {
    /**
     * Resolves the effective DFA depending on the missing transition handling setting.
     */
    val effectiveDfa: DfaModel
        get() = if (missingTransitionMode == MissingTransitionHandling.ADD_TRAP_STATE) {
            dfa.withImplicitTrapState()
        } else {
            dfa
        }
}

enum class ViewMode {
    GRAPH,
    TABLE
}

enum class AppTab {
    SOLVER,
    VISUALIZER,
    STEP_BY_STEP,
    PRESETS,
    ABOUT
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        // Initial auto-solve so data is immediately available
        solveDfa()
    }

    fun setDfa(newDfa: DfaModel) {
        val validation = newDfa.validate()
        _uiState.update {
            it.copy(
                dfa = newDfa,
                validationResult = validation,
                solution = null,
                currentGnfa = null,
                verificationSummary = null,
                customEliminationOrder = newDfa.states
            )
        }
        if (validation.isValid) {
            solveDfa()
        }
    }

    fun setMissingTransitionMode(mode: MissingTransitionHandling) {
        _uiState.update { it.copy(missingTransitionMode = mode) }
        solveDfa()
        val message = if (mode == MissingTransitionHandling.ADD_TRAP_STATE) {
            "Implicit trap state (q_trap) enabled for missing transitions."
        } else {
            "Missing transitions treated as ∅ (no path)."
        }
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }

    fun addTransition(from: String, symbol: String, to: String) {
        val currentDfa = _uiState.value.dfa
        val newTransitions = currentDfa.transitions.toMutableMap()
        newTransitions[Pair(from, symbol)] = to
        setDfa(currentDfa.copy(transitions = newTransitions))
    }

    fun removeTransition(from: String, symbol: String) {
        val currentDfa = _uiState.value.dfa
        val newTransitions = currentDfa.transitions.filterKeys { it != Pair(from, symbol) }
        setDfa(currentDfa.copy(transitions = newTransitions))
    }

    fun loadPreset(preset: DfaPreset) {
        setDfa(preset.dfa)
        Toast.makeText(getApplication(), "Loaded preset: ${preset.title}", Toast.LENGTH_SHORT).show()
    }

    fun generateRandomDfa(stateCount: Int, alphabetSize: Int, finalCount: Int, solveImmediately: Boolean = false) {
        val symbols = if (alphabetSize == 1) listOf("0") else if (alphabetSize == 2) listOf("0", "1") else listOf("a", "b", "c")
        val randomDfa = PresetsData.generateRandomDfa(
            stateCount = stateCount,
            alphabetSymbols = symbols,
            finalStateCount = finalCount
        )
        setDfa(randomDfa)
        if (solveImmediately) {
            setWorkflowStep(4)
        }
        Toast.makeText(getApplication(), "Generated random DFA (${stateCount} states)", Toast.LENGTH_SHORT).show()
    }

    fun setWorkflowStep(step: Int) {
        _uiState.update { it.copy(currentWorkflowStep = step.coerceIn(0, 4)) }
        if (step >= 2 && _uiState.value.currentGnfa == null) {
            val gnfa = GnfaModel.fromDfa(_uiState.value.effectiveDfa)
            _uiState.update { it.copy(currentGnfa = gnfa) }
        }
    }

    fun setActiveTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleLearningMode() {
        _uiState.update { it.copy(isLearningModeEnabled = !it.isLearningModeEnabled) }
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(currentViewMode = mode) }
    }

    fun setManualMode(isManual: Boolean) {
        val gnfa = GnfaModel.fromDfa(_uiState.value.effectiveDfa)
        _uiState.update {
            it.copy(
                isManualMode = isManual,
                currentGnfa = gnfa,
                manualGnfaHistory = listOf(Pair(gnfa, "Initial GNFA"))
            )
        }
    }

    fun eliminateManualState(state: String) {
        val currentGnfa = _uiState.value.currentGnfa ?: return
        val (nextGnfa, _) = StateEliminationEngine.eliminateSingleState(
            gnfa = currentGnfa,
            qk = state,
            stepNumber = _uiState.value.manualGnfaHistory.size
        )
        val newHistory = _uiState.value.manualGnfaHistory + Pair(nextGnfa, "Eliminated $state")
        _uiState.update {
            it.copy(
                currentGnfa = nextGnfa,
                manualGnfaHistory = newHistory
            )
        }

        // If only q_start and q_accept remain, solve is complete!
        if (nextGnfa.eliminableStates().isEmpty()) {
            solveDfa()
            _uiState.update { it.copy(currentWorkflowStep = 4) }
            Toast.makeText(getApplication(), "All states eliminated! Final regex ready.", Toast.LENGTH_SHORT).show()
        }
    }

    fun solveDfa(customOrder: List<String>? = null) {
        val effectiveDfa = _uiState.value.effectiveDfa
        val validation = effectiveDfa.validate()
        if (!validation.isValid) {
            _uiState.update { it.copy(validationResult = validation) }
            return
        }

        val solution = StateEliminationEngine.solve(effectiveDfa, customOrder)
        val verification = VerificationEngine.verify(effectiveDfa, solution.finalRegex)

        _uiState.update {
            it.copy(
                solution = solution,
                currentGnfa = solution.finalGnfa,
                verificationSummary = verification,
                activeStepIndex = 0
            )
        }
    }

    fun setActiveStepIndex(index: Int) {
        val stepsCount = _uiState.value.solution?.steps?.size ?: 0
        if (stepsCount > 0) {
            _uiState.update { it.copy(activeStepIndex = index.coerceIn(0, stepsCount - 1)) }
        }
    }

    fun resetAll() {
        val defaultPreset = PresetsData.PRESETS.first()
        setDfa(defaultPreset.dfa)
        _uiState.update {
            it.copy(
                currentWorkflowStep = 0,
                activeTab = AppTab.SOLVER,
                isManualMode = false,
                missingTransitionMode = MissingTransitionHandling.TREAT_AS_NO_PATH
            )
        }
        solveDfa()
        Toast.makeText(getApplication(), "Reset to default state.", Toast.LENGTH_SHORT).show()
    }

    fun copyToClipboard(text: String, label: String = "Regex") {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "Copied $label to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun exportSolutionText(): String {
        val sol = _uiState.value.solution ?: return "No solution available."
        return buildString {
            appendLine("=== STATE ELIMINATION METHOD SOLUTION ===")
            appendLine("Application: State Elimination Tool")
            appendLine()
            appendLine("1. Original DFA:")
            appendLine("States: ${sol.initialDfa.states.joinToString(", ")}")
            appendLine("Alphabet: ${sol.initialDfa.alphabet.joinToString(", ")}")
            appendLine("Start State: ${sol.initialDfa.startState}")
            appendLine("Final States: ${sol.initialDfa.finalStates.joinToString(", ")}")
            appendLine("Transitions (${sol.initialDfa.transitions.size}):")
            for ((key, target) in sol.initialDfa.transitions) {
                appendLine("  δ(${key.first}, '${key.second}') = $target")
            }
            appendLine()
            appendLine("2. Elimination Order: ${sol.eliminationOrder.joinToString(" → ")}")
            appendLine()
            appendLine("3. Step-by-Step Derivation:")
            for (step in sol.steps) {
                appendLine("--- Step ${step.stepNumber}: Eliminate State ${step.eliminatedState} ---")
                appendLine("Formula: Rij = Rij | Rik(Rkk)*Rkj")
                appendLine("Self loop on ${step.eliminatedState}: ${step.selfLoop}")
                for (up in step.updates) {
                    appendLine("  Path ${up.fromState} → ${up.toState}: New = ${up.newTransition} (was ${up.oldTransition})")
                }
                appendLine()
            }
            appendLine("4. Final Equivalent Regular Expression:")
            appendLine(sol.finalRegex.toString())
            appendLine()
            val ver = _uiState.value.verificationSummary
            if (ver != null) {
                appendLine("5. Empirical Verification: ${ver.passedCount}/${ver.totalTested} test strings matched.")
            }
        }
    }
}
