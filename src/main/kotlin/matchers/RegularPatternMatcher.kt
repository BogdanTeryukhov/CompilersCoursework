package matchers

import util.*

class RegularPatternMatcher : BasicMatcher, WordPatternGenerator {

    fun isRegular(pattern: Pattern): Boolean {
        val counts = mutableMapOf<String, Int>()
        for (e in pattern) {
            if (e is Variable) {
                counts[e.name] = counts.getOrDefault(e.name, 0) + 1
            }
        }
        return counts.values.all { it == 1 }
    }

    @MeasureTime
    override fun match(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>
    ): Substitution? {

        if (!isRegular(pattern)) {
            throw IllegalArgumentException("Pattern is not regular")
        }

        var pPos = 0
        var wPos = 0

        while (pPos < pattern.size) {
            val el = pattern[pPos]

            when (el) {
                is Terminal -> {
                    if (wPos >= word.length || word[wPos] != el.symbol) {
                        return null
                    }
                    pPos++
                    wPos++
                }
                is Variable -> {
                    val name = el.name

                    val next = if (pPos + 1 < pattern.size) {
                        pattern[pPos + 1]
                    } else null

                    val endPos = when (next) {
                        is Terminal -> {
                            word.indexOf(next.symbol, wPos).also {
                                if (it == -1) return null
                            }
                        }
                        is Variable -> {
                            if (wPos >= word.length) return null
                            wPos + 1
                        }
                        else -> word.length
                    }

                    val value = word.substring(wPos, endPos)
                    substitution[name] = value

                    wPos = endPos
                    pPos++
                }
            }
        }

        return if (wPos == word.length) substitution else null
    }

    override fun generateWordAndPattern(
        numOfVars: Int,
        alphabet: String
    ): Pair<String, String> {
        val subword = (1..6).map { alphabet.random() }.joinToString("")

        val pattern: StringBuilder = StringBuilder()
        val word: StringBuilder = StringBuilder()

        for (i in 1..numOfVars) {
            pattern.append("x$i $subword ")
            word.append("TFL$i $subword ")
        }
        return (word.toString() to pattern.toString())
    }
}
