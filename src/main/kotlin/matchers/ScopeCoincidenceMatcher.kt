package matchers

import util.*

class ScopeCoincidenceMatcher(private val maxSCD: Int) : BasicMatcher, WordPatternGenerator {

    @MeasureTime
    override fun match(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String>
    ): Substitution? {

        val scd = calculateSCD(pattern)
        if (scd > maxSCD) {
            throw IllegalArgumentException(
                "util.Pattern scd = $scd, but max value = $maxSCD"
            )
        }

        if (isSubstitutionComplete(pattern, substitution)) {
            return if (PatternParser.applySubstitution(pattern, substitution) == word) substitution else null
        }

        val nextVariable = findNextUnassignedVariable(pattern, substitution)
        if (nextVariable == null) return null

        val possibleValues = generatePossibleValues(pattern, nextVariable, word, substitution)

        for (value in possibleValues) {
            substitution[nextVariable] = value
            val result = match(pattern, word, substitution)
            if (result != null) return result
            substitution.remove(nextVariable)
        }

        return null
    }

    private fun isSubstitutionComplete(pattern: Pattern, substitution: Map<String, String>): Boolean {
        val variablesInPattern = pattern.filterIsInstance<Variable>().map { it.name }.toSet()
        return variablesInPattern.all { it in substitution }
    }

    private fun findNextUnassignedVariable(pattern: Pattern, substitution: Map<String, String>): String? {
        return pattern.filterIsInstance<Variable>().map { it.name }.find { it !in substitution }
    }

    private fun generatePossibleValues(
        pattern: Pattern,
        variable: String,
        word: Word,
        substitution: Map<String, String>
    ): List<String> {
        val tempSub = substitution.toMutableMap()
        val possibleValues = mutableListOf<String>()

        val maxLength = word.length

        for (length in 1..maxLength) {
            tempSub[variable] = "A".repeat(length) // заглушка для расчета длины

            val result = PatternParser.applySubstitution(pattern, tempSub)
            if (result.length > word.length) break

            for (start in 0..(word.length - length)) {
                val candidate = word.substring(start, start + length)

                // проверяем, что candidate согласуется со всеми вхождениями переменной
                if (isCandidateConsistent(pattern, variable, candidate, substitution)) {
                    possibleValues.add(candidate)
                }
            }
        }

        return possibleValues.distinct()
    }

    private fun isCandidateConsistent(
        pattern: Pattern,
        variable: String,
        candidate: String,
        substitution: Map<String, String>
    ): Boolean {
        val tempSub = substitution.toMutableMap()
        tempSub[variable] = candidate

        val result = PatternParser.applySubstitution(pattern, tempSub)

        return result.length >= candidate.length
    }

    fun calculateSCD(pattern: Pattern): Int {
        val variableRanges = mutableMapOf<String, IntRange>()

        pattern.forEachIndexed { index, element ->
            if (element is Variable) {
                val currentScope = variableRanges[element.name]
                if (currentScope == null) {
                    variableRanges[element.name] = index..index
                } else {
                    variableRanges[element.name] = currentScope.first..index
                }
            }
        }

        var maxSCD = 0

        for (i in pattern.indices) {
            var count = 0
            for (range in variableRanges.values) {
                if (i in range) {
                    count++
                }
            }
            maxSCD = maxOf(maxSCD, count)
        }

        return maxSCD
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