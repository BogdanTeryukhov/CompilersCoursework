package matchers

import util.*

class ScopeCoincidenceMatcher(private val maxSCD: Int) : BasicMatcher, WordPatternGenerator {

    fun calculateSCD(pattern: Pattern): Int {
        val activeSets = computeActiveSets(pattern)
        return activeSets.maxOf { it.size }
    }

    @MeasureTime
    override fun match(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>
    ): Substitution? {

        val activeSets = computeActiveSets(pattern)

        if (activeSets.any { it.size > maxSCD }) {
            throw IllegalArgumentException(
                "Pattern SCD exceeds limit $maxSCD"
            )
        }

        return matchFrom(pattern, word, substitution, activeSets, 0, 0)
    }

    private fun matchFrom(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>,
        activeSets: List<Set<String>>,
        pPos: Int,
        wPos: Int
    ): Substitution? {
        if (pPos == pattern.size) {
            return if (wPos == word.length) substitution else null
        }

        val el = pattern[pPos]

        return when (el) {
            is Terminal -> {
                if (wPos < word.length && word[wPos] == el.symbol) {
                    matchFrom(
                        pattern, word, substitution,
                        activeSets,
                        pPos + 1, wPos + 1
                    )
                } else null
            }
            is Variable -> {
                val name = el.name
                val current = substitution[name]
                if (current != null) {
                    if (word.startsWith(current, wPos)) {
                        matchFrom(
                            pattern, word, substitution,
                            activeSets,
                            pPos + 1, wPos + current.length
                        )
                    } else null
                } else {
                    val minRest = minimalRemainingLength(pattern, pPos + 1, substitution)
                    val maxLen = word.length - wPos - minRest
                    if (maxLen < 0) return null

                    for (len in 0..maxLen) {
                        val value = word.substring(wPos, wPos + len)
                        substitution[name] = value

                        val res = matchFrom(
                            pattern,
                            word,
                            substitution,
                            activeSets,
                            pPos + 1,
                            wPos + len
                        )
                        if (res != null) return res

                        substitution.remove(name)
                    }
                    null
                }
            }
        }
    }

    private fun minimalRemainingLength(
        pattern: Pattern,
        start: Int,
        substitution: Map<String, String>
    ): Int {
        var len = 0
        for (i in start until pattern.size) {
            when (val e = pattern[i]) {
                is Terminal -> len += 1
                is Variable -> substitution[e.name]?.let { len += it.length }
            }
        }
        return len
    }

    private fun computeActiveSets(
        pattern: Pattern
    ): List<Set<String>> {

        val first = mutableMapOf<String, Int>()
        val last = mutableMapOf<String, Int>()

        for ((i, e) in pattern.withIndex()) {
            if (e is Variable) {
                first.putIfAbsent(e.name, i)
                last[e.name] = i
            }
        }

        return pattern.indices.map { i ->
            first.keys.filter { v ->
                i in first[v]!!..last[v]!!
            }.toSet()
        }
    }

    override fun generateWordAndPattern(
        numOfVars: Int,
        alphabet: String
    ): Pair<String, String> {

        val pattern = StringBuilder()
        val word = StringBuilder()

        repeat(numOfVars) {
            pattern.append("x1 x2 ")
        }
        pattern.append("a")

        repeat(numOfVars * 2) {
            word.append("a ")
        }
        word.append("b")

        return word.toString().trim() to pattern.toString().trim()
    }
}
