package matchers

import PatternParser.applySubstitution
import util.Pattern
import util.Substitution
import util.Variable
import util.Word

class RepeatedVariablesMatcher(private val maxRepeatedVars: Int) {

    fun match(pattern: Pattern, word: Word): Substitution? {
        val repeatedCount = calculateRepeatedVariablesCount(pattern)

        if (repeatedCount > maxRepeatedVars) {
            throw IllegalArgumentException(
                "Шаблон содержит $repeatedCount повторяющихся переменных, " +
                        "что превышает максимально допустимое $maxRepeatedVars"
            )
        }

        val variableStats = analyzeVariables(pattern)
        val (repeatedVars, singleVars) = partitionVariables(variableStats)
        return optimizedMatch(pattern, word, repeatedVars, singleVars, mutableMapOf())
    }

    private fun optimizedMatch(
        pattern: Pattern,
        word: Word,
        repeatedVars: List<String>,
        singleVars: List<String>,
        substitution: MutableMap<String, String>
    ): Substitution? {

        val allVariables = repeatedVars + singleVars
        if (substitution.keys.containsAll(allVariables)) {
            val result = applySubstitution(pattern, substitution)
            return if (result == word) substitution else null
        }

        // обработка повторяющихся переменных
        val nextRepeatedVar = repeatedVars.find { it !in substitution }
        if (nextRepeatedVar != null) {
            val stats = analyzeVariables(pattern)[nextRepeatedVar]!!
            val possibleValues = generateValuesForRepeatedVar(pattern, nextRepeatedVar, stats, word, substitution)

            for (value in possibleValues) {
                substitution[nextRepeatedVar] = value
                val result = optimizedMatch(pattern, word, repeatedVars, singleVars, substitution)
                if (result != null) return result
                substitution.remove(nextRepeatedVar)
            }
            return null
        }

        // обработка одиночных переменных
        val nextSingleVar = singleVars.find { it !in substitution } ?: return null

        for (start in 0 until word.length) {
            for (end in start + 1..word.length) {
                val candidate = word.substring(start, end)
                substitution[nextSingleVar] = candidate

                val result = optimizedMatch(pattern, word, repeatedVars, singleVars, substitution)
                if (result != null) return result

                substitution.remove(nextSingleVar)
            }
        }

        return null
    }

    private fun generateValuesForRepeatedVar(
        pattern: Pattern,
        variable: String,
        stats: VariableStats,
        word: Word,
        substitution: Map<String, String>
    ): List<String> {
        val possibleValues = mutableListOf<String>()
        val occurrencesCount = stats.count

        // ограничиваем максимальную длину
        val maxLength = minOf(word.length / occurrencesCount, 50)

        for (length in 1..maxLength) {
            for (start in 0..(word.length - length)) {
                val candidate = word.substring(start, start + length)
                if (isValueValidForAllOccurrences(pattern, variable, candidate, substitution)) {
                    possibleValues.add(candidate)
                }
            }
        }

        return possibleValues.distinct()
    }

    private fun isValueValidForAllOccurrences(
        pattern: Pattern,
        variable: String,
        value: String,
        substitution: Map<String, String>
    ): Boolean {
        val tempSub = substitution.toMutableMap()
        tempSub[variable] = value

        val result = applySubstitution(pattern, tempSub)

        return result.length <= 1000
    }

    data class VariableStats(val count: Int, val positions: List<Int>, val isRepeated: Boolean)

    private fun analyzeVariables(pattern: Pattern): Map<String, VariableStats> {
        val stats = mutableMapOf<String, MutableList<Int>>()

        pattern.forEachIndexed { index, element ->
            if (element is Variable) {
                stats.getOrPut(element.name) { mutableListOf() }.add(index)
            }
        }

        return stats.mapValues { (name, positions) ->
            VariableStats(positions.size, positions.sorted(), positions.size > 1)
        }
    }

    private fun partitionVariables(variableStats: Map<String, VariableStats>): Pair<List<String>, List<String>> {
        val repeated = variableStats.filter { it.value.isRepeated }.keys.toList()
        val single = variableStats.filter { !it.value.isRepeated }.keys.toList()
        return Pair(repeated, single)
    }

    fun calculateRepeatedVariablesCount(pattern: Pattern): Int {
        val counts = mutableMapOf<String, Int>()

        pattern.forEach { element ->
            if (element is Variable) {
                counts[element.name] = counts.getOrDefault(element.name, 0) + 1
            }
        }

        return counts.values.count { it > 1 }
    }
}