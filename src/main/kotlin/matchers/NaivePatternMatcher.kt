package matchers

import util.*

class NaivePatternMatcher : BasicMatcher {

    @MeasureTime
    override fun match(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>
    ): Substitution? {
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
                    for (len in 0..(word.length - wPos)) {
                        val candidate = word.substring(wPos, wPos + len)
                        substitution[name] = candidate
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
}
