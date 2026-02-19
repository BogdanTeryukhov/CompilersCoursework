package matchers.Repv

import util.BasicMatcher
import util.MeasureTime
import util.Pattern
import util.Substitution
import util.Terminal
import util.Variable
import util.Word
import util.WordPatternGenerator

class RepeatedVariablesMatcher(private val maxRepeatedVars: Int) : BasicMatcher, WordPatternGenerator {

    fun calculateRepeatedVariablesCount(pattern: Pattern): Int {
        val counts = mutableMapOf<String, Int>()
        for (e in pattern) {
            if (e is Variable) {
                counts[e.name] = counts.getOrDefault(e.name, 0) + 1
            }
        }
        val repeated = counts.values.count { it > 1 }
        if (repeated > maxRepeatedVars) {
            throw IllegalArgumentException(
                "Шаблон содержит $repeated повторяющихся переменных, что превышает допустимое $maxRepeatedVars"
            )
        }
        return repeated
    }

    @MeasureTime
    override fun match(pattern: Pattern, word: Word, substitution: MutableMap<String, String>): Substitution? {
        val counts = countVars(pattern)
        val repeated = counts.filter { it.value > 1 }.keys
        if (repeated.size > maxRepeatedVars) return null

        val vars = counts.keys.toList() // переменные
        val occ = vars.map { counts[it]!! } // сколько раз встретилась каждая
        val terminals = pattern.count { it is Terminal } // сколько терминалов

        val lens = IntArray(vars.size) // длины значений для каждой переменной (изначально 0)

        fun bt(i: Int, rest: Int): Substitution? { // i - индекс текущей переменной, rest - сколько символов осталось распределить
            if (i == vars.size) {
                if (rest != 0) return null // если не все символы распределены
                return check(pattern, word, vars, lens)
            }
            val c = occ[i] // сколько раз встречается переменная vars[i]
            var l = 0
            while (c * l <= rest) { // количество вхождений * длина <= оставшихся символов
                lens[i] = l
                val r = bt(i + 1, rest - c * l)
                if (r != null) return r
                l++
            }
            return null
        }

        return bt(0, word.length - terminals)
    }

    private fun check(
        pattern: Pattern,
        word: Word,
        vars: List<String>,
        lens: IntArray
    ): Substitution? {

        val hash = RollingHash(word)
        val assigned = mutableMapOf<String, Pair<Long, Int>>()
        val result = mutableMapOf<String, String>()

        var pos = 0
        for (e in pattern) {
            when (e) {
                is Terminal -> {
                    if (pos >= word.length || word[pos] != e.symbol) return null
                    pos++
                }
                is Variable -> {
                    val i = vars.indexOf(e.name)
                    val len = lens[i]
                    if (pos + len > word.length) return null

                    val h = hash.getHash(pos, pos + len)

                    val prev = assigned[e.name]
                    if (prev == null) {
                        assigned[e.name] = h to len
                        result[e.name] = word.substring(pos, pos + len)
                    } else {
                        if (prev.first != h || prev.second != len) return null
                    }
                    pos += len
                }
            }
        }
        return if (pos == word.length) result else null
    }

    private fun countVars(pattern: Pattern): Map<String, Int> {
        val m = mutableMapOf<String, Int>()
        for (e in pattern) if (e is Variable) m[e.name] = m.getOrDefault(e.name, 0) + 1
        return m
    }

    // паттерн вида x1 a x1 a x1 a x1 a x1 a x1 a x1 a x1 a x1 a x1 a b, слово вида aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa c
    override fun generateWordAndPattern(
        numOfVars: Int,
        alphabet: String
    ): Pair<String, String> {

        val repetitions = numOfVars * 5
        val xValue = "a".repeat(numOfVars)

        val pattern = StringBuilder()
        val word = StringBuilder()

        repeat(repetitions) {
            pattern.append("x1 a ")
            word.append(xValue).append("a ")
        }

        pattern.append("b")
        word.append("c")

        return word.toString().trim() to pattern.toString().trim()
    }
}