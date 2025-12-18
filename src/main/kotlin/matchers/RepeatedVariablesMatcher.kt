package matchers

import util.*


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

        val vars = counts.keys.toList()
        val occ = vars.map { counts[it]!! }
        val terminals = pattern.count { it is Terminal }

        val lens = IntArray(vars.size)

        fun bt(i: Int, rest: Int): Substitution? {
            if (i == vars.size) {
                if (rest != 0) return null
                return check(pattern, word, vars, lens)
            }
            val c = occ[i]
            var l = 0
            while (c * l <= rest) {
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
        val sub = mutableMapOf<String, String>()
        var p = 0
        for (e in pattern) {
            when (e) {
                is Terminal -> {
                    if (p >= word.length || word[p] != e.symbol) return null
                    p++
                }
                is Variable -> {
                    val i = vars.indexOf(e.name)
                    val l = lens[i]
                    if (p + l > word.length) return null
                    val s = word.substring(p, p + l)
                    val prev = sub[e.name]
                    if (prev == null) sub[e.name] = s
                    else if (prev != s) return null
                    p += l
                }
            }
        }
        return if (p == word.length) sub else null
    }

    private fun countVars(pattern: Pattern): Map<String, Int> {
        val m = mutableMapOf<String, Int>()
        for (e in pattern) if (e is Variable) m[e.name] = m.getOrDefault(e.name, 0) + 1
        return m
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
