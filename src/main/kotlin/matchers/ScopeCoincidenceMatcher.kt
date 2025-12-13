package matchers

import util.*

class ScopeCoincidenceMatcher(private val maxSCD: Int) : BasicMatcher, WordPatternGenerator {

    @MeasureTime
    override fun match(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>
    ): Substitution? {

        val scd = calculateSCD(pattern)
        if (scd > maxSCD) {
            throw IllegalArgumentException(
                "Pattern scd = $scd, but max value = $maxSCD"
            )
        }

        return matchFrom(
            pattern = pattern,
            word = word,
            pPos = 0,
            wPos = 0,
            substitution = substitution
        )
    }

    private fun matchFrom(
        pattern: Pattern,
        word: Word,
        pPos: Int,
        wPos: Int,
        substitution: MutableMap<String, String>
    ): Substitution? {

        if (pPos == pattern.size) {
            return if (wPos == word.length) substitution else null
        }

        val el = pattern[pPos]

        return when (el) {
            is Terminal -> {
                if (wPos < word.length && word[wPos] == el.symbol) {
                    matchFrom(pattern, word, pPos + 1, wPos + 1, substitution)
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
                            pPos + 1,
                            wPos + current.length,
                            substitution
                        )
                    } else null
                } else {
                    for (len in 1..(word.length - wPos)) {
                        val candidate = word.substring(wPos, wPos + len)
                        substitution[name] = candidate
                        val res = matchFrom(
                            pattern,
                            word,
                            pPos + 1,
                            wPos + len,
                            substitution
                        )
                        if (res != null) return res
                        substitution.remove(name)
                    }
                    null
                }
            }
        }
    }

    fun calculateSCD(pattern: Pattern): Int {
        val ranges = mutableMapOf<String, IntRange>()

        pattern.forEachIndexed { i, el ->
            if (el is Variable) {
                val r = ranges[el.name]
                ranges[el.name] = if (r == null) i..i else r.first..i
            }
        }

        var max = 0
        for (i in pattern.indices) {
            var c = 0
            for (r in ranges.values) {
                if (i in r) c++
            }
            max = maxOf(max, c)
        }
        return max
    }

    override fun generateWordAndPattern(
        numOfVars: Int,
        alphabet: String
    ): Pair<String, String> {
        val variablesList = listOf("x1 ", "x2 ", "x3 ", "x4 ", "x5 ", "x6 ")
        val wordPrefix = listOf("TFL1 ", "TFL2 ", "TFL3 ", "TFL4 ", "TFL5 ", "TFL6 ")

        val subword = (1..6).map { alphabet.random() }.joinToString("")

        val pattern: StringBuilder = StringBuilder()
        val word: StringBuilder = StringBuilder()

        for (i in 1..numOfVars) {
            pattern.append(variablesList[i - 1] + subword + " ")
            word.append(wordPrefix[i - 1] + subword + " ")
        }
        return word.toString().trim() to pattern.toString().trim()
    }
}
