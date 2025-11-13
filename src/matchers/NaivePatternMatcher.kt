package matchers

import PatternParser.applySubstitution
import util.Pattern
import util.Substitution
import util.Variable
import util.Word

class NaivePatternMatcher {

    fun match(pattern: Pattern, word: Word): Substitution? {
        val variables = pattern.filterIsInstance<Variable>().map { it.name }.distinct()
        return naiveMatch(pattern, word, variables, mutableMapOf(), 0)
    }

    private fun naiveMatch(
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
        for (start in 0..word.length) {
            for (end in start..word.length) {
                val candidate = word.substring(start, end)
                substitution[currentVar] = candidate

                val result = naiveMatch(pattern, word, variables, substitution, varIndex + 1)
                if (result != null) return result
            }
        }

        substitution.remove(currentVar)
        return null
    }
}