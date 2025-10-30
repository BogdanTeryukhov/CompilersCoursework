package matchers

import util.*

import PatternParser.applySubstitution

class RepeatedVariablesMatcher(private val maxRepeatedVars: Int) {

    fun match(pattern: Pattern, word: Word): Substitution? {
        val repeatedCount = calculateRepeatedVariablesCount(pattern)
        if (repeatedCount > maxRepeatedVars) {
            throw IllegalArgumentException(
                "util.Pattern contains $repeatedCount repeated variables, but max is $maxRepeatedVars"
            )
        }

        val variables = pattern.filterIsInstance<Variable>().map { it.name }.distinct()
        return bruteForceMatch(pattern, word, variables, mutableMapOf(), 0)
    }

    private fun bruteForceMatch(
        pattern: Pattern,
        word: Word,
        variables: List<String>,
        substitution: MutableMap<String, String>,
        varIndex: Int
    ): Substitution? {

        if (varIndex == variables.size) {
            val result = applySubstitution(pattern, substitution)
            return if (result == word) substitution else null
        }

        val currentVar = variables[varIndex]

        for (start in 0 until word.length) {
            for (end in start + 1..word.length) {
                val candidate = word.substring(start, end)
                substitution[currentVar] = candidate

                val result = bruteForceMatch(pattern, word, variables, substitution, varIndex + 1)
                if (result != null) return result
            }
        }

        substitution.remove(currentVar)
        return null
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