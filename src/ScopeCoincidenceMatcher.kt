class ScopeCoincidenceMatcher {

    fun match(
        pattern: Pattern,
        word: Word,
        substitution: MutableMap<String, String> = mutableMapOf()
    ): Substitution? {

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
}
