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

        // конец шаблона (если дошли до конца шаблона, проверяем, дошли ли до конца слова)
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
                val current = substitution[name]

                if (current != null) { // если уже есть инфа по значению переменной
                    // проверка, что в слове на этой позиции именно эта строка
                    if (word.startsWith(current, wPos)) {
                        matchFrom(
                            pattern,
                            word,
                            substitution,
                            pPos + 1,
                            wPos + current.length // шагаем на то кол-во символов вперед, сколько в подстановке
                        )
                    } else null
                } else {
                    // пробуем все возможные подстроки от текущей позиции
                    for (len in 0..(word.length - wPos)) {
                        // подстрока длины len
                        val candidate = word.substring(wPos, wPos + len)
                        // пробуем подставить
                        substitution[name] = candidate

                        val res = matchFrom(
                            pattern,
                            word,
                            substitution,
                            pPos + 1,
                            wPos + len
                        )
                        // если нашли решение - возвращаем
                        if (res != null) return res
                        // иначе ролбэк
                        substitution.remove(name)
                    }
                    null
                }
            }
        }
    }
}
