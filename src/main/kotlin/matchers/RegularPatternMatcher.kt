package matchers

import util.BasicMatcher
import util.MeasureTime
import util.Pattern
import util.Substitution
import util.Terminal
import util.Variable
import util.Word
import util.WordPatternGenerator

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

        val variables = mutableListOf<String>()
        val terminals = mutableListOf<String>()

        val current = StringBuilder()
        for (el in pattern) {
            when (el) {
                is Terminal -> current.append(el.symbol)
                is Variable -> {
                    terminals.add(current.toString())
                    current.clear()
                    variables.add(el.name)
                }
            }
        }
        terminals.add(current.toString())

        if (variables.isEmpty()) {
            return if (terminals[0] == word) substitution else null
        }

        if (!word.startsWith(terminals[0])) return null
        var pos = terminals[0].length

        val suffix = terminals.last()
        if (!word.endsWith(suffix)) return null

        for (i in 0 until variables.size - 1) {
            val nextTerminal = terminals[i + 1]

            val endPos = if (nextTerminal.isNotEmpty()) {
                val idx = word.indexOf(nextTerminal, pos)
                if (idx == -1) return null
                idx
            } else {
                if (pos >= word.length) return null
                pos + 1
            }

            substitution[variables[i]] = word.substring(pos, endPos)
            pos = endPos + nextTerminal.length
        }

        val lastVar = variables.last()
        val endPos = word.length - suffix.length
        if (endPos < pos) return null

        substitution[lastVar] = word.substring(pos, endPos)
        return substitution
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