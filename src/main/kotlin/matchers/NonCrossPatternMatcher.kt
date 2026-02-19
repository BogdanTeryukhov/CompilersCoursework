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
                // текущий символ
                if (wPos < word.length && word[wPos] == el.symbol) {
                    // если совпал, то некст
                    matchFrom(pattern, word, substitution, pPos + 1, wPos + 1)
                } else null
            }

            is Variable -> {
                val name = el.name

                // сколько раз подряд идет переменная
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
                // если переменная уже определена
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

                for (len in 0..maxLen) {
                    val segmentLen = len * count
                    if (wPos + segmentLen > word.length) break

                    // подстрока длины len
                    val value = word.substring(wPos, wPos + len)

                    // проверка, что следующие count-1 фрагментов слова точно такие же, как первый
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

    fun isNonCrossPattern(pattern: Pattern): Boolean {
        // запоминаем от первой до последней позиции для каждой переменной
        val scopes = mutableMapOf<String, IntRange>()

        pattern.forEachIndexed { idx, el ->
            if (el is Variable) {
                val currentScope = scopes[el.name]  // получаем текущий диапазон для переменной

                if (currentScope != null) {
                    // переменная уже встречалась раньше
                    // расширяем диапазон до текущей позиции
                    scopes[el.name] = currentScope.first..idx
                } else {
                    // переменная встречается впервые
                    // создаем диапазон из одной позиции
                    scopes[el.name] = idx..idx
                }
            }
        }

        val sorted = scopes.values.sortedBy { it.first }

        // проверка, что диапазоны не пересекаются
        for (i in 1 until sorted.size) {
            if (sorted[i - 1].last >= sorted[i].first) return false
        }
        return true
    }

    // паттерн вида x2 ov x2 ov, слово вида TFL2 ov TFL2 ov
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
