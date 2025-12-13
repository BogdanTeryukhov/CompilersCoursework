package matchers

import util.*

class RegularPatternMatcher : BasicMatcher, WordPatternGenerator {

    fun isRegular(pattern: Pattern): Boolean {
        val variableCount = mutableMapOf<String, Int>()
        pattern.forEach { element ->
            if (element is Variable) {
                variableCount[element.name] = variableCount.getOrDefault(element.name, 0) + 1
            }
        }
        return variableCount.values.all { it == 1 }
    }

    @MeasureTime
    override fun match(pattern: Pattern, word: Word, substitution: MutableMap<String, String>): Substitution? {
        if (!isRegular(pattern)) {
            throw IllegalArgumentException("util.Pattern is not regular")
        }

        return matchRecursive(pattern, word, 0, 0, mutableMapOf())
    }

    private fun matchRecursive(
        pattern: Pattern,
        word: Word,
        patternIndex: Int,
        wordIndex: Int,
        currentSubstitution: MutableMap<String, String>
    ): Substitution? {

        if (patternIndex == pattern.size) {
            return if (wordIndex == word.length) currentSubstitution else null
        }

        val currentElement = pattern[patternIndex]

        return when (currentElement) {
            is Terminal -> {
                if (wordIndex < word.length && word[wordIndex] == currentElement.symbol) {
                    matchRecursive(pattern, word, patternIndex + 1, wordIndex + 1, currentSubstitution)
                } else {
                    null
                }
            }

            is Variable -> {
                val nextTerminalIndex = findNextTerminal(pattern, patternIndex + 1)
                val maxLength = if (nextTerminalIndex != -1) {
                    // позиция следующего терминала в слове
                    val terminal = pattern[nextTerminalIndex] as Terminal
                    val terminalPos = word.indexOf(terminal.symbol, wordIndex)
                    if (terminalPos == -1) return null
                    terminalPos - wordIndex
                } else {
                    word.length - wordIndex
                }

                for (length in 1..maxLength) {
                    if (wordIndex + length > word.length) break

                    val variableValue = word.substring(wordIndex, wordIndex + length)
                    val newSubstitution = currentSubstitution.toMutableMap()
                    newSubstitution[currentElement.name] = variableValue

                    val result = matchRecursive(
                        pattern, word, patternIndex + 1,
                        wordIndex + length, newSubstitution
                    )

                    if (result != null) return result
                }
                null
            }
        }
    }

    private fun findNextTerminal(pattern: Pattern, startIndex: Int): Int {
        for (i in startIndex until pattern.size) {
            if (pattern[i] is Terminal) return i
        }
        return -1
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
        return (word.toString() to pattern.toString())
    }
}