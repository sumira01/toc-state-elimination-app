package com.example.model

import java.util.regex.Pattern

/**
 * Abstract Syntax Tree (AST) representing a Regular Expression.
 * Supports mathematical operations, safe algebraic simplifications,
 * string formatting with operator precedence, and regex pattern compilation.
 */
sealed class RegexNode {
    abstract val precedence: Int

    /** Empty set (∅) - language with zero strings */
    data object EmptySet : RegexNode() {
        override val precedence: Int = 4
        override fun toString(): String = "∅"
    }

    /** Empty string (ε) - language with only the empty string */
    data object Epsilon : RegexNode() {
        override val precedence: Int = 4
        override fun toString(): String = "ε"
    }

    /** Single alphabet character / symbol (e.g. "0", "1", "a") */
    data class Literal(val symbol: String) : RegexNode() {
        override val precedence: Int = 4
        override fun toString(): String = symbol
    }

    /** Union / Alternation (R1 | R2) */
    data class Union(val left: RegexNode, val right: RegexNode) : RegexNode() {
        override val precedence: Int = 1
        override fun toString(): String {
            val terms = collectUnionTerms(this)
            return terms.joinToString("|") { child ->
                if (child.precedence < precedence) "($child)" else child.toString()
            }
        }
    }

    /** Concatenation (R1 · R2) */
    data class Concat(val left: RegexNode, val right: RegexNode) : RegexNode() {
        override val precedence: Int = 2
        override fun toString(): String {
            val terms = collectConcatTerms(this)
            return terms.joinToString("") { child ->
                if (child.precedence < precedence) "($child)" else child.toString()
            }
        }
    }

    /** Kleene Star (R*) */
    data class Star(val inner: RegexNode) : RegexNode() {
        override val precedence: Int = 3
        override fun toString(): String {
            val innerStr = if (inner.precedence < precedence) "($inner)" else inner.toString()
            return "$innerStr*"
        }
    }

    companion object {
        fun collectUnionTerms(node: RegexNode): List<RegexNode> {
            val list = mutableListOf<RegexNode>()
            fun collect(n: RegexNode) {
                if (n is Union) {
                    collect(n.left)
                    collect(n.right)
                } else {
                    list.add(n)
                }
            }
            collect(node)
            return list
        }

        fun collectConcatTerms(node: RegexNode): List<RegexNode> {
            val list = mutableListOf<RegexNode>()
            fun collect(n: RegexNode) {
                if (n is Concat) {
                    collect(n.left)
                    collect(n.right)
                } else {
                    list.add(n)
                }
            }
            collect(node)
            return list
        }

        /**
         * Safely simplifies a regular expression using standard algebraic identities:
         * - ∅ | R = R, R | ∅ = R
         * - R | R = R (idempotence)
         * - ε · R = R, R · ε = R
         * - ∅ · R = ∅, R · ∅ = ∅
         * - ∅* = ε, ε* = ε, (R*)* = R*
         * - (R | ε)* = R*
         */
        fun simplify(node: RegexNode): RegexNode {
            return when (node) {
                is EmptySet, is Epsilon, is Literal -> node

                is Star -> {
                    val inner = simplify(node.inner)
                    when {
                        inner is EmptySet -> Epsilon
                        inner is Epsilon -> Epsilon
                        inner is Star -> inner
                        inner is Union -> {
                            val terms = collectUnionTerms(inner).filter { it !is EmptySet }
                            val withoutEps = terms.filter { it !is Epsilon }
                            if (withoutEps.isEmpty()) {
                                Epsilon
                            } else if (withoutEps.size == 1) {
                                Star(withoutEps.first())
                            } else {
                                val rebuilt = withoutEps.reduce { acc, cur -> Union(acc, cur) }
                                Star(rebuilt)
                            }
                        }
                        else -> Star(inner)
                    }
                }

                is Concat -> {
                    val left = simplify(node.left)
                    val right = simplify(node.right)
                    when {
                        left is EmptySet || right is EmptySet -> EmptySet
                        left is Epsilon -> right
                        right is Epsilon -> left
                        else -> {
                            // Flatten and combine adjacent concatenations if any
                            Concat(left, right)
                        }
                    }
                }

                is Union -> {
                    val left = simplify(node.left)
                    val right = simplify(node.right)
                    when {
                        left is EmptySet -> right
                        right is EmptySet -> left
                        left == right -> left
                        else -> {
                            val allTerms = (collectUnionTerms(left) + collectUnionTerms(right))
                                .filter { it !is EmptySet }
                                .distinct()

                            if (allTerms.isEmpty()) {
                                EmptySet
                            } else if (allTerms.size == 1) {
                                allTerms.first()
                            } else {
                                allTerms.reduce { acc, cur -> Union(acc, cur) }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Helper operators to build expressions fluently
         */
        fun union(left: RegexNode, right: RegexNode): RegexNode =
            simplify(Union(left, right))

        fun concat(left: RegexNode, right: RegexNode): RegexNode =
            simplify(Concat(left, right))

        fun star(inner: RegexNode): RegexNode =
            simplify(Star(inner))
    }

    /**
     * Translates the AST into a Java regex pattern for testing against input strings.
     */
    fun toJavaRegexPattern(): String {
        return when (this) {
            is EmptySet -> "(?!)" // Never matches
            is Epsilon -> ""
            is Literal -> Pattern.quote(symbol)
            is Union -> "(?:" + left.toJavaRegexPattern() + "|" + right.toJavaRegexPattern() + ")"
            is Concat -> "(?:" + left.toJavaRegexPattern() + ")(?:" + right.toJavaRegexPattern() + ")"
            is Star -> "(?:" + inner.toJavaRegexPattern() + ")*"
        }
    }

    /**
     * Matches a string against this regex AST.
     */
    fun matches(input: String): Boolean {
        return try {
            val regex = "^" + toJavaRegexPattern() + "$"
            Pattern.compile(regex).matcher(input).matches()
        } catch (_: Exception) {
            false
        }
    }
}
