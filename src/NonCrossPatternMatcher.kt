class NonCrossPatternMatcher {

    data class PatternSegment(
        val variables: List<VariableBlock> = emptyList(),
        val terminals: String = ""
    )

    data class VariableBlock(val variable: String, val count: Int)

    fun match(pattern: Pattern, word: Word): Substitution? {
        if (!isNonCrossPattern(pattern)) {
            throw IllegalArgumentException("Pattern is not non cross")
        }

        val segments = parsePatternIntoSegments(pattern)

        return matchSegmentsRecursive(segments, word, 0, 0, mutableMapOf())
    }

    // парсит шаблон на сегменты [переменные][терминалы][переменные][терминалы]...
    private fun parsePatternIntoSegments(pattern: Pattern): List<PatternSegment> {
        val segments = mutableListOf<PatternSegment>()
        var currentVariables = mutableListOf<VariableBlock>()
        val currentTerminals = StringBuilder()

        var i = 0
        while (i < pattern.size) {
            when (val element = pattern[i]) {
                is Terminal -> {
                    // если были переменные, сохраняем их как отдельный сегмент
                    if (currentVariables.isNotEmpty()) {
                        segments.add(PatternSegment(variables = currentVariables.toList()))
                        currentVariables = mutableListOf()
                    }
                    currentTerminals.append(element.symbol)
                    i++
                }
                is Variable -> {
                    // если были терминалы, сохраняем их как отдельный сегмент
                    if (currentTerminals.isNotEmpty()) {
                        segments.add(PatternSegment(terminals = currentTerminals.toString()))
                        currentTerminals.clear()
                    }
                    // cобираем блок одной переменной
                    val variableName = element.name
                    var count = 0
                    var j = i
                    while (j < pattern.size && pattern[j] is Variable &&
                        (pattern[j] as Variable).name == variableName) {
                        count++
                        j++
                    }
                    currentVariables.add(VariableBlock(variableName, count))
                    i = j
                }
            }
        }

        if (currentVariables.isNotEmpty()) {
            segments.add(PatternSegment(variables = currentVariables.toList()))
        }
        if (currentTerminals.isNotEmpty()) {
            segments.add(PatternSegment(terminals = currentTerminals.toString()))
        }

        return segments
    }

    private fun matchSegmentsRecursive(
        segments: List<PatternSegment>,
        word: Word,
        segmentIndex: Int,
        wordPos: Int,
        substitution: MutableMap<String, String>
    ): Substitution? {

        if (segmentIndex == segments.size) {
            return if (wordPos == word.length) substitution else null
        }

        val segment = segments[segmentIndex]

        return if (segment.variables.isNotEmpty()) {
            // cегмент с переменными
            matchVariableSegment(segments, word, segmentIndex, wordPos, substitution, segment)
        } else {
            // cегмент с терминалами
            matchTerminalSegment(segments, word, segmentIndex, wordPos, substitution, segment)
        }
    }

    private fun matchVariableSegment(
        segments: List<PatternSegment>,
        word: Word,
        segmentIndex: Int,
        wordPos: Int,
        substitution: MutableMap<String, String>,
        segment: PatternSegment
    ): Substitution? {

        return matchVariablesInSegment(
            segment.variables,
            segments, word, segmentIndex, wordPos, substitution,
            0, wordPos
        )
    }

    private fun matchVariablesInSegment(
        variables: List<VariableBlock>,
        segments: List<PatternSegment>,
        word: Word,
        segmentIndex: Int,
        initialWordPos: Int,
        substitution: MutableMap<String, String>,
        variableIndex: Int,
        currentWordPos: Int
    ): Substitution? {

        if (variableIndex == variables.size) {
            return matchSegmentsRecursive(segments, word, segmentIndex + 1, currentWordPos, substitution)
        }

        val block = variables[variableIndex]
        val currentValue = substitution[block.variable]

        return if (currentValue != null) {
            matchExistingVariableInSegment(
                variables, segments, word, segmentIndex, initialWordPos,
                substitution, variableIndex, currentWordPos, block, currentValue
            )
        } else {
            matchNewVariableInSegment(
                variables, segments, word, segmentIndex, initialWordPos,
                substitution, variableIndex, currentWordPos, block
            )
        }
    }

    private fun matchExistingVariableInSegment(
        variables: List<VariableBlock>,
        segments: List<PatternSegment>,
        word: Word,
        segmentIndex: Int,
        initialWordPos: Int,
        substitution: MutableMap<String, String>,
        variableIndex: Int,
        currentWordPos: Int,
        block: VariableBlock,
        currentValue: String
    ): Substitution? {
        val expectedSegment = currentValue.repeat(block.count)

        if (currentWordPos + expectedSegment.length > word.length) {
            return null
        }

        val actualSegment = word.substring(currentWordPos, currentWordPos + expectedSegment.length)
        if (actualSegment != expectedSegment) {
            return null
        }

        return matchVariablesInSegment(
            variables, segments, word, segmentIndex, initialWordPos,
            substitution, variableIndex + 1, currentWordPos + expectedSegment.length
        )
    }

    private fun matchNewVariableInSegment(
        variables: List<VariableBlock>,
        segments: List<PatternSegment>,
        word: Word,
        segmentIndex: Int,
        initialWordPos: Int,
        substitution: MutableMap<String, String>,
        variableIndex: Int,
        currentWordPos: Int,
        block: VariableBlock
    ): Substitution? {
        val maxLength = calculateMaxVariableLengthForSegment(
            variables, segments, segmentIndex, variableIndex, word, currentWordPos
        )

        for (length in 1..maxLength) {
            if (currentWordPos + (length * block.count) > word.length) break

            val candidate = word.substring(currentWordPos, currentWordPos + length)

            var valid = true
            for (i in 1 until block.count) {
                val segmentStart = currentWordPos + (i * length)
                val segmentEnd = segmentStart + length
                if (segmentEnd > word.length ||
                    word.substring(segmentStart, segmentEnd) != candidate) {
                    valid = false
                    break
                }
            }

            if (!valid) continue

            substitution[block.variable] = candidate
            val newWordPos = currentWordPos + (candidate.length * block.count)

            val result = matchVariablesInSegment(
                variables, segments, word, segmentIndex, initialWordPos,
                substitution, variableIndex + 1, newWordPos
            )

            if (result != null) {
                return result
            }

            substitution.remove(block.variable)
        }
        return null
    }

    private fun matchTerminalSegment(
        segments: List<PatternSegment>,
        word: Word,
        segmentIndex: Int,
        wordPos: Int,
        substitution: MutableMap<String, String>,
        segment: PatternSegment
    ): Substitution? {

        val terminals = segment.terminals
        if (wordPos + terminals.length > word.length) {
            return null
        }

        val wordSegment = word.substring(wordPos, wordPos + terminals.length)
        if (wordSegment != terminals) {
            return null
        }

        return matchSegmentsRecursive(segments, word, segmentIndex + 1, wordPos + terminals.length, substitution)
    }

    private fun calculateMaxVariableLengthForSegment(
        variables: List<VariableBlock>,
        segments: List<PatternSegment>,
        segmentIndex: Int,
        variableIndex: Int,
        word: Word,
        currentWordPos: Int
    ): Int {
        var remainingLength = 0

        for (i in variableIndex until variables.size) {
            remainingLength += variables[i].count // мин1 символ на вхождение
        }

        for (i in segmentIndex + 1 until segments.size) {
            val segment = segments[i]
            if (segment.terminals.isNotEmpty()) {
                remainingLength += segment.terminals.length
            } else {
                for (block in segment.variables) {
                    remainingLength += block.count // мин 1 символ на вхождение
                }
            }
        }

        val availableLength = word.length - currentWordPos - remainingLength
        return availableLength.coerceAtLeast(1)
    }

    fun isNonCrossPattern(pattern: Pattern): Boolean {
        val scopes = mutableMapOf<String, IntRange>()

        pattern.forEachIndexed { index, element ->
            if (element is Variable) {
                val currentScope = scopes[element.name]
                if (currentScope == null) {
                    scopes[element.name] = index..index
                } else {
                    scopes[element.name] = currentScope.first..index
                }
            }
        }

        val sortedScopes = scopes.values.sortedBy { it.first }
        for (i in 1 until sortedScopes.size) {
            if (sortedScopes[i - 1].last >= sortedScopes[i].first) {
                return false
            }
        }

        return true
    }
}