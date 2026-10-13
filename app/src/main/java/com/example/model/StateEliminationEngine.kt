package com.example.model

/**
 * Details of how a single transition between qi and qj was updated
 * during the elimination of qk.
 */
data class PathUpdateInfo(
    val fromState: String,
    val toState: String,
    val oldTransition: RegexNode,
    val rIk: RegexNode,
    val rKkStar: RegexNode,
    val rKj: RegexNode,
    val bypassPath: RegexNode,
    val newTransition: RegexNode
) {
    val formulaString: String
        get() {
            val bypassStr = when {
                rKkStar is RegexNode.Epsilon -> "$rIk$rKj"
                else -> "$rIk($rKkStar)$rKj"
            }
            return if (oldTransition is RegexNode.EmptySet) {
                bypassStr
            } else {
                "$oldTransition | $bypassStr"
            }
        }
}

/**
 * Record of a single state elimination step with before/after state graphs
 * and detailed mathematical derivations.
 */
data class EliminationStep(
    val stepNumber: Int,
    val eliminatedState: String,
    val gnfaBefore: GnfaModel,
    val gnfaAfter: GnfaModel,
    val selfLoop: RegexNode,
    val incomingStates: List<Pair<String, RegexNode>>,
    val outgoingStates: List<Pair<String, RegexNode>>,
    val updates: List<PathUpdateInfo>,
    val explanation: String
)

/**
 * Full solution result from converting DFA -> GNFA -> State Elimination -> Final Regex.
 */
data class SolutionResult(
    val initialDfa: DfaModel,
    val initialGnfa: GnfaModel,
    val steps: List<EliminationStep>,
    val finalRegex: RegexNode,
    val finalGnfa: GnfaModel,
    val eliminationOrder: List<String>
)

object StateEliminationEngine {

    /**
     * Solves the DFA completely using either a provided elimination order
     * or the default order of states.
     */
    fun solve(dfa: DfaModel, customOrder: List<String>? = null): SolutionResult {
        val initialGnfa = GnfaModel.fromDfa(dfa)
        val eliminable = initialGnfa.eliminableStates()

        val order = if (customOrder != null && customOrder.size == eliminable.size && customOrder.toSet() == eliminable.toSet()) {
            customOrder
        } else {
            // Default natural order: q0, q1, q2...
            eliminable
        }

        var currentGnfa = initialGnfa
        val steps = mutableListOf<EliminationStep>()

        for ((index, stateToEliminate) in order.withIndex()) {
            val (nextGnfa, step) = eliminateSingleState(currentGnfa, stateToEliminate, index + 1)
            steps.add(step)
            currentGnfa = nextGnfa
        }

        val finalRegex = currentGnfa.getTransition(GnfaModel.START_STATE, GnfaModel.ACCEPT_STATE)

        return SolutionResult(
            initialDfa = dfa,
            initialGnfa = initialGnfa,
            steps = steps,
            finalRegex = finalRegex,
            finalGnfa = currentGnfa,
            eliminationOrder = order
        )
    }

    /**
     * Eliminates a single state qk from a GNFA, returning the new GNFA and the step metadata.
     */
    fun eliminateSingleState(
        gnfa: GnfaModel,
        qk: String,
        stepNumber: Int
    ): Pair<GnfaModel, EliminationStep> {
        val remainingStates = gnfa.states.filter { it != qk }

        val rKk = gnfa.getTransition(qk, qk)
        val rKkStar = RegexNode.star(rKk)

        // Find incoming states (i != qk where R_ik != ∅)
        val incoming = remainingStates.filter { i ->
            i != GnfaModel.ACCEPT_STATE && gnfa.getTransition(i, qk) !is RegexNode.EmptySet
        }.map { i -> Pair(i, gnfa.getTransition(i, qk)) }

        // Find outgoing states (j != qk where R_kj != ∅)
        val outgoing = remainingStates.filter { j ->
            j != GnfaModel.START_STATE && gnfa.getTransition(qk, j) !is RegexNode.EmptySet
        }.map { j -> Pair(j, gnfa.getTransition(qk, j)) }

        val newTransitions = mutableMapOf<Pair<String, String>, RegexNode>()
        val updates = mutableListOf<PathUpdateInfo>()

        // Update transitions for all pairs (qi, qj) in remainingStates
        for (i in remainingStates) {
            for (j in remainingStates) {
                // q_start has no incoming, q_accept has no outgoing
                if (j == GnfaModel.START_STATE || i == GnfaModel.ACCEPT_STATE) {
                    newTransitions[Pair(i, j)] = RegexNode.EmptySet
                    continue
                }

                val oldRij = gnfa.getTransition(i, j)
                val rIk = gnfa.getTransition(i, qk)
                val rKj = gnfa.getTransition(qk, j)

                // Bypass path: R_ik · (R_kk)* · R_kj
                val bypass = RegexNode.simplify(
                    RegexNode.Concat(
                        rIk,
                        RegexNode.Concat(rKkStar, rKj)
                    )
                )

                // New transition: R_ij = R_ij | bypass
                val newRij = RegexNode.union(oldRij, bypass)
                newTransitions[Pair(i, j)] = newRij

                // Record updates that are meaningful (where either old or bypass is non-empty)
                if (bypass !is RegexNode.EmptySet || oldRij !is RegexNode.EmptySet) {
                    updates.add(
                        PathUpdateInfo(
                            fromState = i,
                            toState = j,
                            oldTransition = oldRij,
                            rIk = rIk,
                            rKkStar = rKkStar,
                            rKj = rKj,
                            bypassPath = bypass,
                            newTransition = newRij
                        )
                    )
                }
            }
        }

        val nextGnfa = GnfaModel(
            states = remainingStates,
            startState = gnfa.startState,
            acceptState = gnfa.acceptState,
            transitions = newTransitions
        )

        val explanation = buildString {
            append("Eliminated state $qk. ")
            if (rKk is RegexNode.EmptySet) {
                append("State $qk has no self-loop. ")
            } else {
                append("State $qk loops on itself with ($rKk)*. ")
            }
            append("All paths entering $qk (${incoming.size} source(s)) and exiting $qk (${outgoing.size} destination(s)) ")
            append("have been redirected using formula: Rij = Rij | Rik(Rkk)*Rkj.")
        }

        val step = EliminationStep(
            stepNumber = stepNumber,
            eliminatedState = qk,
            gnfaBefore = gnfa,
            gnfaAfter = nextGnfa,
            selfLoop = rKk,
            incomingStates = incoming,
            outgoingStates = outgoing,
            updates = updates,
            explanation = explanation
        )

        return Pair(nextGnfa, step)
    }
}
