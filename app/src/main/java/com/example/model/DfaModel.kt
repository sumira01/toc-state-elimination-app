package com.example.model

/**
 * Representation of a Deterministic Finite Automaton (DFA).
 * Supports both complete DFAs and partial/incomplete transition diagrams,
 * where states may have missing transitions (treated as ∅ / no path).
 */
data class DfaModel(
    val states: List<String> = listOf("q0", "q1"),
    val alphabet: List<String> = listOf("0", "1"),
    val startState: String = "q0",
    val finalStates: Set<String> = setOf("q1"),
    val transitions: Map<Pair<String, String>, String> = mapOf(
        Pair("q0", "0") to "q0",
        Pair("q0", "1") to "q1",
        Pair("q1", "0") to "q0",
        Pair("q1", "1") to "q1"
    )
) {
    /**
     * Validates the DFA.
     * Partial/incomplete transitions are valid input and NOT treated as errors.
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()
        val infoMessages = mutableListOf<String>()

        if (states.isEmpty()) {
            errors.add("DFA must have at least one state.")
        }
        if (alphabet.isEmpty()) {
            errors.add("Alphabet must contain at least one symbol.")
        }
        if (startState.isBlank() || !states.contains(startState)) {
            errors.add("Start state '$startState' must belong to the set of states.")
        }
        if (finalStates.isEmpty()) {
            errors.add("DFA must have at least one final (accepting) state.")
        }
        for (f in finalStates) {
            if (!states.contains(f)) {
                errors.add("Final state '$f' is not in the set of states.")
            }
        }

        // Validate existing transitions
        for ((key, target) in transitions) {
            val (from, sym) = key
            if (!states.contains(from)) {
                errors.add("Transition source state '$from' does not exist.")
            }
            if (!states.contains(target)) {
                errors.add("Transition target state '$target' does not exist.")
            }
            if (!alphabet.contains(sym)) {
                errors.add("Transition symbol '$sym' is not in the alphabet {${alphabet.joinToString()}}.")
            }
        }

        // Check if there are any undefined/missing transitions
        var isComplete = true
        var missingCount = 0
        for (state in states) {
            for (symbol in alphabet) {
                if (!transitions.containsKey(Pair(state, symbol))) {
                    isComplete = false
                    missingCount++
                }
            }
        }

        if (!isComplete && errors.isEmpty()) {
            infoMessages.add(
                "Some transitions are not defined ($missingCount undefined). They will be treated as ∅ (no path) during GNFA and state elimination."
            )
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            infoMessages = infoMessages,
            isCompleteDfa = isComplete
        )
    }

    /**
     * Simulates the DFA on an input string.
     * Returns true if the string ends in a final state, false if it ends in non-final
     * or encounters an undefined transition (no path).
     */
    fun simulate(input: String): Boolean {
        var current = startState
        for (char in input) {
            val symbol = char.toString()
            val next = transitions[Pair(current, symbol)] ?: return false // No path -> rejects
            current = next
        }
        return finalStates.contains(current)
    }

    /**
     * Returns a new DfaModel where all undefined transitions are directed to an implicit trap state.
     */
    fun withImplicitTrapState(): DfaModel {
        val validation = validate()
        if (validation.isCompleteDfa) return this

        var trapStateName = "q_trap"
        var counter = 1
        while (states.contains(trapStateName)) {
            trapStateName = "q_trap_$counter"
            counter++
        }

        val newStates = states + trapStateName
        val newTransitions = transitions.toMutableMap()

        // Route all missing transitions from original states to trap state
        for (state in states) {
            for (sym in alphabet) {
                if (!newTransitions.containsKey(Pair(state, sym))) {
                    newTransitions[Pair(state, sym)] = trapStateName
                }
            }
        }

        // Trap state loops on itself for all alphabet symbols
        for (sym in alphabet) {
            newTransitions[Pair(trapStateName, sym)] = trapStateName
        }

        return copy(
            states = newStates,
            transitions = newTransitions
        )
    }

    /**
     * Returns all transitions originating from a given state.
     */
    fun getTransitionsFrom(state: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (symbol in alphabet) {
            transitions[Pair(state, symbol)]?.let { target ->
                map[symbol] = target
            }
        }
        return map
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val infoMessages: List<String> = emptyList(),
    val isCompleteDfa: Boolean = true
)
