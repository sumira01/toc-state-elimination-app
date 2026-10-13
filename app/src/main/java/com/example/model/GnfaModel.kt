package com.example.model

/**
 * Representation of a Generalized Nondeterministic Finite Automaton (GNFA).
 * In a GNFA:
 * - There is exactly one start state q_start with no incoming transitions.
 * - There is exactly one accept state q_accept with no outgoing transitions.
 * - q_start != q_accept.
 * - Every transition between any two states (u, v) is labeled by a Regular Expression (RegexNode).
 *   Missing edges are represented by RegexNode.EmptySet (∅).
 */
data class GnfaModel(
    val states: List<String>,
    val startState: String = START_STATE,
    val acceptState: String = ACCEPT_STATE,
    val transitions: Map<Pair<String, String>, RegexNode>
) {
    companion object {
        const val START_STATE = "q_start"
        const val ACCEPT_STATE = "q_accept"

        /**
         * Converts a validated DFA into its equivalent initial GNFA.
         */
        fun fromDfa(dfa: DfaModel): GnfaModel {
            // New distinct start and accept states
            val gnfaStates = mutableListOf<String>()
            gnfaStates.add(START_STATE)
            gnfaStates.addAll(dfa.states)
            gnfaStates.add(ACCEPT_STATE)

            val transitionMap = mutableMapOf<Pair<String, String>, RegexNode>()

            // 1. Group original transitions between existing states u -> v
            val dfaEdges = mutableMapOf<Pair<String, String>, MutableList<String>>()
            for (u in dfa.states) {
                for (v in dfa.states) {
                    dfaEdges[Pair(u, v)] = mutableListOf()
                }
            }

            for ((key, target) in dfa.transitions) {
                val (from, symbol) = key
                if (dfa.states.contains(from) && dfa.states.contains(target)) {
                    dfaEdges[Pair(from, target)]?.add(symbol)
                }
            }

            // Populate transitions between original states
            for (u in dfa.states) {
                for (v in dfa.states) {
                    val symbols = dfaEdges[Pair(u, v)] ?: emptyList()
                    val regex: RegexNode = when {
                        symbols.isEmpty() -> RegexNode.EmptySet
                        symbols.size == 1 -> RegexNode.Literal(symbols.first())
                        else -> {
                            val distinct = symbols.distinct()
                            distinct.map { RegexNode.Literal(it) as RegexNode }
                                .reduce { acc, cur -> RegexNode.Union(acc, cur) }
                        }
                    }
                    transitionMap[Pair(u, v)] = RegexNode.simplify(regex)
                }
            }

            // 2. Add transition from q_start to original start state with ε
            for (v in gnfaStates) {
                if (v == dfa.startState) {
                    transitionMap[Pair(START_STATE, v)] = RegexNode.Epsilon
                } else {
                    transitionMap[Pair(START_STATE, v)] = RegexNode.EmptySet
                }
            }

            // 3. Add transitions from original accept states to q_accept with ε
            for (u in gnfaStates) {
                if (dfa.finalStates.contains(u)) {
                    transitionMap[Pair(u, ACCEPT_STATE)] = RegexNode.Epsilon
                } else {
                    transitionMap[Pair(u, ACCEPT_STATE)] = RegexNode.EmptySet
                }
            }

            // 4. Ensure no incoming to q_start and no outgoing from q_accept
            for (u in gnfaStates) {
                transitionMap[Pair(u, START_STATE)] = RegexNode.EmptySet
                transitionMap[Pair(ACCEPT_STATE, u)] = RegexNode.EmptySet
            }

            return GnfaModel(
                states = gnfaStates,
                startState = START_STATE,
                acceptState = ACCEPT_STATE,
                transitions = transitionMap
            )
        }
    }

    /**
     * Retrieves the transition regular expression between state u and state v.
     * Returns RegexNode.EmptySet if no transition exists.
     */
    fun getTransition(u: String, v: String): RegexNode {
        return transitions[Pair(u, v)] ?: RegexNode.EmptySet
    }

    /**
     * Returns all states that can be eliminated (i.e. neither q_start nor q_accept).
     */
    fun eliminableStates(): List<String> {
        return states.filter { it != startState && it != acceptState }
    }

    /**
     * Returns all active non-empty transitions for visualization.
     */
    fun getActiveTransitions(): List<GnfaEdge> {
        val list = mutableListOf<GnfaEdge>()
        for ((pair, regex) in transitions) {
            val (u, v) = pair
            if (regex !is RegexNode.EmptySet) {
                list.add(GnfaEdge(from = u, to = v, label = regex))
            }
        }
        return list
    }
}

data class GnfaEdge(
    val from: String,
    val to: String,
    val label: RegexNode
)
