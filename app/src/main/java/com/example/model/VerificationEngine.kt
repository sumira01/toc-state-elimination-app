package com.example.model

/**
 * Result of evaluating a single test string against both DFA and Regular Expression.
 */
data class TestStringResult(
    val inputString: String,
    val displayInput: String, // "ε (empty string)" or actual string
    val dfaAccepted: Boolean,
    val regexAccepted: Boolean,
    val isMatch: Boolean
)

data class VerificationSummary(
    val results: List<TestStringResult>,
    val totalTested: Int,
    val passedCount: Int,
    val allPassed: Boolean,
    val note: String = "Empirical verification using generated test strings."
)

object VerificationEngine {

    /**
     * Generates a representative set of test strings up to length 3-4 using the alphabet,
     * and evaluates each against both the DFA and the generated Regular Expression.
     */
    fun verify(dfa: DfaModel, regex: RegexNode): VerificationSummary {
        val testStrings = generateTestStrings(dfa.alphabet, maxLength = 3)
        val results = mutableListOf<TestStringResult>()

        for (str in testStrings) {
            val dfaAccepted = dfa.simulate(str)
            val regexAccepted = regex.matches(str)
            val isMatch = (dfaAccepted == regexAccepted)

            results.add(
                TestStringResult(
                    inputString = str,
                    displayInput = if (str.isEmpty()) "ε (empty)" else "\"$str\"",
                    dfaAccepted = dfaAccepted,
                    regexAccepted = regexAccepted,
                    isMatch = isMatch
                )
            )
        }

        val passedCount = results.count { it.isMatch }
        return VerificationSummary(
            results = results,
            totalTested = results.size,
            passedCount = passedCount,
            allPassed = passedCount == results.size
        )
    }

    /**
     * Systematically generates test strings: empty string, length 1, length 2, length 3,
     * plus selected patterns.
     */
    private fun generateTestStrings(alphabet: List<String>, maxLength: Int): List<String> {
        val strings = mutableListOf<String>()
        strings.add("") // ε

        var currentLengthStrings = listOf("")
        for (len in 1..maxLength) {
            val nextList = mutableListOf<String>()
            for (prefix in currentLengthStrings) {
                for (sym in alphabet) {
                    val candidate = prefix + sym
                    nextList.add(candidate)
                    if (strings.size < 32) {
                        strings.add(candidate)
                    }
                }
            }
            currentLengthStrings = nextList
            if (strings.size >= 32) break
        }

        return strings.distinct()
    }
}
