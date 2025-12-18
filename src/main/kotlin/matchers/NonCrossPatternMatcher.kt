package matchers

import util.*

class NonCrossPatternMatcher : BasicMatcher, WordPatternGenerator {

    @MeasureTime
    override fun match(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>
    ): Substitution? {

        if (!isNonCrossPattern(pattern)) {
            throw IllegalArgumentException("Pattern is not non-cross")
        }

        var pPos = 0
        var wPos = 0

        while (pPos < pattern.size) {
            when (val el = pattern[pPos]) {
                is Terminal -> {
                    if (wPos >= word.length || word[wPos] != el.symbol) {
                        return null
                    }
                    pPos++
                    wPos++
                }
                is Variable -> {
                    val name = el.name
                    var count = 0
                    var j = pPos
                    while (j < pattern.size &&
                        pattern[j] is Variable &&
                        (pattern[j] as Variable).name == name
                    ) {
                        count++
                        j++
                    }

                    val existing = substitution[name]

                    if (existing != null) {
                        val expected = existing.repeat(count)
                        if (!word.startsWith(expected, wPos)) return null
                        wPos += expected.length
                        pPos = j
                        continue
                    }
                    val nextTerminal = if (j < pattern.size && pattern[j] is Terminal) {
                        (pattern[j] as Terminal).symbol
                    } else null

                    val valueLength = when (nextTerminal) {
                        null -> {
                            val remaining = word.length - wPos
                            if (remaining % count != 0) return null
                            remaining / count
                        }
                        else -> {
                            val idx = word.indexOf(nextTerminal, wPos)
                            if (idx == -1) return null
                            val segmentLen = idx - wPos
                            if (segmentLen % count != 0) return null
                            segmentLen / count
                        }
                    }

                    val value = word.substring(wPos, wPos + valueLength)

                    repeat(count) { i ->
                        val start = wPos + i * valueLength
                        val end = start + valueLength
                        if (word.substring(start, end) != value) return null
                    }

                    substitution[name] = value
                    wPos += valueLength * count
                    pPos = j
                }
            }
        }

        return if (wPos == word.length) substitution else null
    }

    fun isNonCrossPattern(pattern: Pattern): Boolean {
        val scopes = mutableMapOf<String, IntRange>()

        pattern.forEachIndexed { idx, el ->
            if (el is Variable) {
                scopes[el.name] = scopes[el.name]?.let {
                    it.first..idx
                } ?: (idx..idx)
            }
        }

        val sorted = scopes.values.sortedBy { it.first }
        for (i in 1 until sorted.size) {
            if (sorted[i - 1].last >= sorted[i].first) return false
        }
        return true
    }

    override fun generateWordAndPattern(
        numOfVars: Int,
        alphabet: String
    ): Pair<String, String> {
        val subword = (1..6).map { alphabet.random() }.joinToString("")

        val pattern: StringBuilder = StringBuilder()
        val word: StringBuilder = StringBuilder()

        for (i in 1..numOfVars) {
            pattern.append("x$numOfVars $subword ")
            word.append("TFL$numOfVars $subword ")
        }
        return (word.toString() to pattern.toString())
    }
}
