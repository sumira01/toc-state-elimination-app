package com.example.model

import kotlin.random.Random

enum class PresetCategory {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

data class DfaPreset(
    val id: String,
    val title: String,
    val description: String,
    val category: PresetCategory,
    val dfa: DfaModel,
    val expectedExplanation: String
)

object PresetsData {

    val PRESETS = listOf(
        // Beginner 1
        DfaPreset(
            id = "ending_with_1",
            title = "Strings ending with '1'",
            description = "Accepts any binary string where the last symbol is '1'.",
            category = PresetCategory.BEGINNER,
            dfa = DfaModel(
                states = listOf("q0", "q1"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q1"),
                transitions = mapOf(
                    Pair("q0", "0") to "q0",
                    Pair("q0", "1") to "q1",
                    Pair("q1", "0") to "q0",
                    Pair("q1", "1") to "q1"
                )
            ),
            expectedExplanation = "Matches any combination of 0s and 1s ending in 1: (0|1)*1"
        ),

        // Beginner 2
        DfaPreset(
            id = "starting_with_0",
            title = "Strings starting with '0'",
            description = "Accepts strings that begin with '0', rejecting anything starting with '1'.",
            category = PresetCategory.BEGINNER,
            dfa = DfaModel(
                states = listOf("q0", "q1", "q_trap"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q1"),
                transitions = mapOf(
                    Pair("q0", "0") to "q1",
                    Pair("q0", "1") to "q_trap",
                    Pair("q1", "0") to "q1",
                    Pair("q1", "1") to "q1",
                    Pair("q_trap", "0") to "q_trap",
                    Pair("q_trap", "1") to "q_trap"
                )
            ),
            expectedExplanation = "Starts with 0 followed by any symbols: 0(0|1)*"
        ),

        // Beginner 3
        DfaPreset(
            id = "containing_1",
            title = "Strings containing at least one '1'",
            description = "Accepts strings that contain symbol '1' somewhere.",
            category = PresetCategory.BEGINNER,
            dfa = DfaModel(
                states = listOf("q0", "q1"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q1"),
                transitions = mapOf(
                    Pair("q0", "0") to "q0",
                    Pair("q0", "1") to "q1",
                    Pair("q1", "0") to "q1",
                    Pair("q1", "1") to "q1"
                )
            ),
            expectedExplanation = "Loops 0s, sees a 1, then accepts everything: 0*1(0|1)*"
        ),

        // Intermediate 4
        DfaPreset(
            id = "even_ones",
            title = "Even number of '1's",
            description = "Accepts binary strings with 0, 2, 4, 6... ones (parity check).",
            category = PresetCategory.INTERMEDIATE,
            dfa = DfaModel(
                states = listOf("q0", "q1"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q0"),
                transitions = mapOf(
                    Pair("q0", "0") to "q0",
                    Pair("q0", "1") to "q1",
                    Pair("q1", "0") to "q1",
                    Pair("q1", "1") to "q0"
                )
            ),
            expectedExplanation = "Even parity of 1s interspersed with 0s: (0|10*1)*"
        ),

        // Intermediate 5
        DfaPreset(
            id = "odd_zeros",
            title = "Odd number of '0's",
            description = "Accepts strings with an odd count of zeros.",
            category = PresetCategory.INTERMEDIATE,
            dfa = DfaModel(
                states = listOf("q0", "q1"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q1"),
                transitions = mapOf(
                    Pair("q0", "0") to "q1",
                    Pair("q0", "1") to "q0",
                    Pair("q1", "0") to "q0",
                    Pair("q1", "1") to "q1"
                )
            ),
            expectedExplanation = "Odd count of 0s: 1*0(1|01*0)*"
        ),

        // Intermediate 6
        DfaPreset(
            id = "contains_01",
            title = "Strings containing substring '01'",
            description = "Accepts strings that contain the sequence '01'.",
            category = PresetCategory.INTERMEDIATE,
            dfa = DfaModel(
                states = listOf("q0", "q1", "q2"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q2"),
                transitions = mapOf(
                    Pair("q0", "0") to "q1",
                    Pair("q0", "1") to "q0",
                    Pair("q1", "0") to "q1",
                    Pair("q1", "1") to "q2",
                    Pair("q2", "0") to "q2",
                    Pair("q2", "1") to "q2"
                )
            ),
            expectedExplanation = "Contains 01: (0|1)*01(0|1)*"
        ),

        // Intermediate 7
        DfaPreset(
            id = "ending_with_00",
            title = "Strings ending with '00'",
            description = "Accepts strings ending strictly with '00'.",
            category = PresetCategory.INTERMEDIATE,
            dfa = DfaModel(
                states = listOf("q0", "q1", "q2"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q2"),
                transitions = mapOf(
                    Pair("q0", "0") to "q1",
                    Pair("q0", "1") to "q0",
                    Pair("q1", "0") to "q2",
                    Pair("q1", "1") to "q0",
                    Pair("q2", "0") to "q2",
                    Pair("q2", "1") to "q0"
                )
            ),
            expectedExplanation = "Strings ending in double zeros: (0|1)*00"
        ),

        // Advanced 8
        DfaPreset(
            id = "contains_101",
            title = "Strings containing substring '101'",
            description = "4-state pattern recognition automaton for '101'.",
            category = PresetCategory.ADVANCED,
            dfa = DfaModel(
                states = listOf("q0", "q1", "q2", "q3"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q3"),
                transitions = mapOf(
                    Pair("q0", "0") to "q0",
                    Pair("q0", "1") to "q1",
                    Pair("q1", "0") to "q2",
                    Pair("q1", "1") to "q1",
                    Pair("q2", "0") to "q0",
                    Pair("q2", "1") to "q3",
                    Pair("q3", "0") to "q3",
                    Pair("q3", "1") to "q3"
                )
            ),
            expectedExplanation = "Matches any string with '101' as substring: (0|1)*101(0|1)*"
        ),

        // Advanced 9
        DfaPreset(
            id = "divisible_by_3",
            title = "Binary numbers divisible by 3",
            description = "Classic modulo-3 automaton tracking remainder of binary value.",
            category = PresetCategory.ADVANCED,
            dfa = DfaModel(
                states = listOf("q0", "q1", "q2"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q0"),
                transitions = mapOf(
                    Pair("q0", "0") to "q0",
                    Pair("q0", "1") to "q1",
                    Pair("q1", "0") to "q2",
                    Pair("q1", "1") to "q0",
                    Pair("q2", "0") to "q1",
                    Pair("q2", "1") to "q2"
                )
            ),
            expectedExplanation = "Binary representation divisible by 3: (0|1(01*0)*1)*"
        ),

        // Advanced 10
        DfaPreset(
            id = "complex_4_state",
            title = "Alternating Tokens Automaton",
            description = "A 4-state DFA with multiple interconnecting paths and cycles.",
            category = PresetCategory.ADVANCED,
            dfa = DfaModel(
                states = listOf("q0", "q1", "q2", "q3"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q2", "q3"),
                transitions = mapOf(
                    Pair("q0", "0") to "q1",
                    Pair("q0", "1") to "q2",
                    Pair("q1", "0") to "q0",
                    Pair("q1", "1") to "q3",
                    Pair("q2", "0") to "q3",
                    Pair("q2", "1") to "q0",
                    Pair("q3", "0") to "q2",
                    Pair("q3", "1") to "q1"
                )
            ),
            expectedExplanation = "Multiple final states and cross-state feedback cycles."
        )
    )

    /**
     * Generates a random valid DFA with the specified parameters.
     */
    fun generateRandomDfa(
        stateCount: Int = 3,
        alphabetSymbols: List<String> = listOf("0", "1"),
        finalStateCount: Int = 1
    ): DfaModel {
        val count = stateCount.coerceIn(2, 6)
        val states = (0 until count).map { "q$it" }
        val startState = states.first()

        val shuffled = states.shuffled(Random)
        val finalCount = finalStateCount.coerceIn(1, count - 1)
        val finalStates = shuffled.take(finalCount).toSet()

        val transitions = mutableMapOf<Pair<String, String>, String>()
        for (state in states) {
            for (sym in alphabetSymbols) {
                val target = states.random(Random)
                transitions[Pair(state, sym)] = target
            }
        }

        return DfaModel(
            states = states,
            alphabet = alphabetSymbols,
            startState = startState,
            finalStates = finalStates,
            transitions = transitions
        )
    }
}
