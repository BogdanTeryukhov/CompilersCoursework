package matchers

import util.*

class ScopeCoincidenceMatcher(private val maxSCD: Int) : BasicMatcher, WordPatternGenerator {

    fun calculateSCD(pattern: Pattern): Int {
        val intervals = mutableMapOf<String, Pair<Int, Int>>()

        for ((i, e) in pattern.withIndex()) {
            if (e is Variable) {
                val cur = intervals[e.name]
                intervals[e.name] =
                    if (cur == null) i to i
                    else cur.first to i
            }
        }

        var scd = 0
        for (i in pattern.indices) {
            val active = intervals.values.count { (l, r) -> i in l..r }
            scd = maxOf(scd, active)
        }
        return scd
    }

    @MeasureTime
    override fun match(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>
    ): Substitution? {
        if (calculateSCD(pattern) > maxSCD) {
            throw IllegalArgumentException("Pattern scd max value = $maxSCD")
        }

        return matchFrom(pattern, word, substitution, 0, 0)
    }

    private fun matchFrom(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>,
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
                    matchFrom(pattern, word, substitution, pPos + 1, wPos + 1)
                } else null
            }
            is Variable -> {
                val name = el.name
                val current = substitution[name]
                if (current != null) {
                    if (word.startsWith(current, wPos)) {
                        matchFrom(
                            pattern,
                            word,
                            substitution,
                            pPos + 1,
                            wPos + current.length
                        )
                    } else null
                } else {
                    val minRest = minimalRemainingLength(pattern, pPos + 1)
                    val maxLen = word.length - wPos - minRest
                    if (maxLen < 0) return null

                    for (len in 1..maxLen) {
                        val value = word.substring(wPos, wPos + len)
                        substitution[name] = value

                        val res = matchFrom(
                            pattern,
                            word,
                            substitution,
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
        start: Int
    ): Int {
        var len = 0
        for (i in start until pattern.size) {
            when (pattern[i]) {
                is Terminal -> len += 1
                is Variable -> len += 1 // минимум 1 символ
            }
        }
        return len
    }

    override fun generateWordAndPattern(
        numOfVars: Int,
        alphabet: String
    ): Pair<String, String> {

        // всегда 3 активные переменные
        val pattern = StringBuilder()
        val word = StringBuilder()

        repeat(numOfVars) {
            pattern.append("x1 x2 x3 ")
            word.append("a b c")
        }

        return word.toString() to pattern.toString()
    }
}
