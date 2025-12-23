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
                    val expectedLen = existing.length * count
                    if (wPos + expectedLen > word.length) return null

                    val expected = existing.repeat(count)
                    if (!word.startsWith(expected, wPos)) return null

                    return matchFrom(
                        pattern,
                        word,
                        substitution,
                        j,
                        wPos + expectedLen
                    )
                }

                val maxLen = maxPossibleLength(
                    pattern,
                    word,
                    j,
                    wPos,
                    count
                )

                // перебор длины ℓ
                for (len in 0..maxLen) {
                    val segmentLen = len * count
                    if (wPos + segmentLen > word.length) break

                    val value = word.substring(wPos, wPos + len)

                    var ok = true
                    for (i in 1 until count) {
                        val start = wPos + i * len
                        val end = start + len
                        if (word.substring(start, end) != value) {
                            ok = false
                            break
                        }
                    }
                    if (!ok) continue

                    substitution[name] = value
                    val res = matchFrom(
                        pattern,
                        word,
                        substitution,
                        j,
                        wPos + segmentLen
                    )
                    if (res != null) return res
                    substitution.remove(name)
                }
                null
            }
        }
    }

    /**
     * Максимально допустимая длина значения переменной,
     * чтобы оставшаяся часть шаблона могла быть сопоставлена.
     */
    private fun maxPossibleLength(
        pattern: Pattern,
        word: Word,
        pPos: Int,
        wPos: Int,
        count: Int
    ): Int {
        var minRemaining = 0
        for (i in pPos until pattern.size) {
            minRemaining += when (pattern[i]) {
                is Terminal -> 1
                is Variable -> 1
            }
        }
        val available = word.length - wPos - minRemaining
        return if (available >= 0) available / count else -1
    }

    /**
     * Проверка непересекающегося.
     * В таком шаблоне области видимости переменных не пересекаются,
     * поэтому в каждый момент времени активна не более одной переменной.
     */
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
