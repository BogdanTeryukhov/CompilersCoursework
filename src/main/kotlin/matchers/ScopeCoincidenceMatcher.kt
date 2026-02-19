package matchers

import util.*

class ScopeCoincidenceMatcher(
    private val maxSCD: Int
) : BasicMatcher, WordPatternGenerator {

    private lateinit var firstPos: Map<String, Int>
    private lateinit var lastPos: Map<String, Int>

    fun calculateSCD(pattern: Pattern): Int {
        computeScopes(pattern)
        return pattern.indices.maxOf { pos ->
            staticActiveAt(pos)
        }
    }

    @MeasureTime
    override fun match(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>
    ): Substitution? {

        val scd = calculateSCD(pattern)
        if (scd > maxSCD) {
            throw IllegalArgumentException(
                "Pattern SCD = $scd exceeds limit $maxSCD"
            )
        }

        return matchFrom(
            pattern, word, substitution,
            pPos = 0, wPos = 0
        )
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

        if (dynamicActiveCount(substitution, pPos) > maxSCD) {
            return null
        }

        return when (val el = pattern[pPos]) {

            is Terminal -> {
                if (wPos < word.length && word[wPos] == el.symbol) {
                    matchFrom(pattern, word, substitution, pPos + 1, wPos + 1)
                } else null
            }
            is Variable -> {
                val name = el.name
                val assigned = substitution[name]

                if (assigned != null) {
                    if (word.startsWith(assigned, wPos)) {
                        matchFrom(pattern, word, substitution, pPos + 1, wPos + assigned.length)
                    } else null
                } else {
                    val minRest = minimalRemainingLength(pattern, pPos + 1, substitution)
                    val maxLen = word.length - wPos - minRest
                    if (maxLen < 0) return null

                    for (len in 0..maxLen) {
                        val value = word.substring(wPos, wPos + len)
                        substitution[name] = value

                        val res = matchFrom(
                            pattern, word, substitution,
                            pPos + 1, wPos + len
                        )
                        if (res != null) return res

                        substitution.remove(name)
                    }
                    null
                }
            }
        }
    }

    // вычисление областей переменных
    private fun computeScopes(pattern: Pattern) {
        val first = mutableMapOf<String, Int>()
        val last = mutableMapOf<String, Int>()

        for ((i, e) in pattern.withIndex()) {
            if (e is Variable) {
                first.putIfAbsent(e.name, i)
                last[e.name] = i
            }
        }

        firstPos = first
        lastPos = last
    }

    // сколько переменных могут быть активны в позиции pos на основе структуры шаблона
    private fun staticActiveAt(pos: Int): Int =
        firstPos.count { (v, l) -> pos in l..lastPos[v]!! }

    // сколько уже назначенных переменных всё ещё активны в текущей позиции pPos
    private fun dynamicActiveCount(
        substitution: Map<String, String>,
        pPos: Int
    ): Int =
        substitution.keys.count { v ->
            pPos <= lastPos[v]!!
        }

    // минимальная длина, которую займет остаток шаблона
    private fun minimalRemainingLength(
        pattern: Pattern,
        start: Int,
        substitution: Map<String, String>
    ): Int {
        var len = 0
        for (i in start until pattern.size) {
            when (val e = pattern[i]) {
                is Terminal -> len += 1
                is Variable ->
                    len += substitution[e.name]?.length ?: 0
            }
        }
        return len
    }

    // паттерн вида x1 x2 x1 x2, слово вида a ba b
    override fun generateWordAndPattern(
        numOfVars: Int,
        alphabet: String
    ): Pair<String, String> {

        val pattern = StringBuilder()
        val word = StringBuilder()

        repeat(numOfVars) {
            pattern.append("x1 x2 ")
            word.append("a b")
        }

        return word.toString().trim() to pattern.toString().trim()
    }
}
