package com.example

import com.example.model.DfaModel
import com.example.model.GnfaModel
import com.example.model.PresetsData
import com.example.model.RegexNode
import com.example.model.StateEliminationEngine
import com.example.model.VerificationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StateEliminationEngineTest {

    @Test
    fun testRegexSimplifications() {
        val r0 = RegexNode.Literal("0")
        val r1 = RegexNode.Literal("1")

        // ∅ | 0 -> 0
        assertEquals("0", RegexNode.union(RegexNode.EmptySet, r0).toString())

        // 0 | 0 -> 0 (idempotent)
        assertEquals("0", RegexNode.union(r0, r0).toString())

        // ε · 1 -> 1
        assertEquals("1", RegexNode.concat(RegexNode.Epsilon, r1).toString())

        // ∅ · 1 -> ∅
        assertEquals("∅", RegexNode.concat(RegexNode.EmptySet, r1).toString())

        // (ε)* -> ε
        assertEquals("ε", RegexNode.star(RegexNode.Epsilon).toString())

        // ((0)*)* -> 0*
        assertEquals("0*", RegexNode.star(RegexNode.star(r0)).toString())
    }

    @Test
    fun testTestA_CompleteDfa() {
        val preset = PresetsData.PRESETS.first()
        val dfa = preset.dfa
        val validation = dfa.validate()

        assertTrue(validation.isValid)
        assertTrue(validation.isCompleteDfa)
        assertTrue(dfa.simulate("1"))
        assertTrue(dfa.simulate("01"))
        assertTrue(dfa.simulate("0011"))
        assertFalse(dfa.simulate("0"))
        assertFalse(dfa.simulate("10"))
    }

    @Test
    fun testTestB_PartialDfaUserExample() {
        // Specific user request example:
        // States: q0, q1
        // Alphabet: 0, 1
        // Start: q0
        // Final: q1
        // Transitions: q0 --0--> q1, q1 --1--> q1
        // (No q0--1--> and no q1--0-->)
        val partialDfa = DfaModel(
            states = listOf("q0", "q1"),
            alphabet = listOf("0", "1"),
            startState = "q0",
            finalStates = setOf("q1"),
            transitions = mapOf(
                Pair("q0", "0") to "q1",
                Pair("q1", "1") to "q1"
            )
        )

        val validation = partialDfa.validate()
        assertTrue("Partial DFA must be valid!", validation.isValid)
        assertFalse(validation.isCompleteDfa)
        assertTrue(validation.infoMessages.isNotEmpty())

        val solution = StateEliminationEngine.solve(partialDfa)
        assertNotNull(solution.finalRegex)
        assertEquals("01*", solution.finalRegex.toString())

        // Check simulation:
        // Accepts: 0, 01, 011, 0111
        // Rejects: 1, 00, 010, ""
        assertTrue(partialDfa.simulate("0"))
        assertTrue(partialDfa.simulate("01"))
        assertTrue(partialDfa.simulate("0111"))
        assertFalse(partialDfa.simulate("1"))
        assertFalse(partialDfa.simulate("00"))
        assertFalse(partialDfa.simulate(""))

        val verification = VerificationEngine.verify(partialDfa, solution.finalRegex)
        assertTrue(verification.allPassed)
    }

    @Test
    fun testTestC_StateWithOnlyOneTransition() {
        // q0 --0--> q1 with no other transitions
        val dfa = DfaModel(
            states = listOf("q0", "q1"),
            alphabet = listOf("0", "1"),
            startState = "q0",
            finalStates = setOf("q1"),
            transitions = mapOf(
                Pair("q0", "0") to "q1"
            )
        )
        val validation = dfa.validate()
        assertTrue(validation.isValid)

        val solution = StateEliminationEngine.solve(dfa)
        assertEquals("0", solution.finalRegex.toString())
    }

    @Test
    fun testTestD_StateWithNoOutgoingTransition() {
        // q0 has outgoing to q1, but q1 (final state) has NO outgoing transitions
        val dfa = DfaModel(
            states = listOf("q0", "q1"),
            alphabet = listOf("a", "b"),
            startState = "q0",
            finalStates = setOf("q1"),
            transitions = mapOf(
                Pair("q0", "a") to "q1"
            )
        )
        val validation = dfa.validate()
        assertTrue(validation.isValid)

        val solution = StateEliminationEngine.solve(dfa)
        assertEquals("a", solution.finalRegex.toString())
    }

    @Test
    fun testTestE_SelfLoop() {
        // q0 (start & final) with self-loop on 1
        val dfa = DfaModel(
            states = listOf("q0"),
            alphabet = listOf("0", "1"),
            startState = "q0",
            finalStates = setOf("q0"),
            transitions = mapOf(
                Pair("q0", "1") to "q0"
            )
        )
        val validation = dfa.validate()
        assertTrue(validation.isValid)

        val solution = StateEliminationEngine.solve(dfa)
        assertEquals("1*", solution.finalRegex.toString())
    }

    @Test
    fun testTestF_MultipleFinalStates() {
        // q0 -> q1 on '0', q0 -> q2 on '1', both q1 and q2 are final
        val dfa = DfaModel(
            states = listOf("q0", "q1", "q2"),
            alphabet = listOf("0", "1"),
            startState = "q0",
            finalStates = setOf("q1", "q2"),
            transitions = mapOf(
                Pair("q0", "0") to "q1",
                Pair("q0", "1") to "q2"
            )
        )
        val validation = dfa.validate()
        assertTrue(validation.isValid)

        val solution = StateEliminationEngine.solve(dfa)
        // Expected regex: 0|1
        assertEquals("0|1", solution.finalRegex.toString())
    }

    @Test
    fun testImplicitTrapState() {
        val partialDfa = DfaModel(
            states = listOf("q0", "q1"),
            alphabet = listOf("0", "1"),
            startState = "q0",
            finalStates = setOf("q1"),
            transitions = mapOf(
                Pair("q0", "0") to "q1",
                Pair("q1", "1") to "q1"
            )
        )
        val completeDfa = partialDfa.withImplicitTrapState()
        assertTrue(completeDfa.states.contains("q_trap"))
        assertTrue(completeDfa.validate().isCompleteDfa)
        assertEquals("q_trap", completeDfa.transitions[Pair("q0", "1")])
        assertEquals("q_trap", completeDfa.transitions[Pair("q1", "0")])
        assertEquals("q_trap", completeDfa.transitions[Pair("q_trap", "0")])
        assertEquals("q_trap", completeDfa.transitions[Pair("q_trap", "1")])
    }
}
