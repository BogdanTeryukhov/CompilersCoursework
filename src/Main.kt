object PatternParser {

    fun parsePattern(patternString: String): Pattern {
        val result = mutableListOf<PatternElement>()
        var i = 0

        while (i < patternString.length) {
            when {
                patternString[i] == 'x' && i + 1 < patternString.length &&
                        patternString[i + 1].isDigit() -> {
                    val varName = buildString {
                        append(patternString[i])
                        i++
                        while (i < patternString.length && patternString[i].isDigit()) {
                            append(patternString[i])
                            i++
                        }
                    }
                    result.add(Variable(varName))
                }
                patternString[i].isLowerCase() || patternString[i].isWhitespace() -> {
                    result.add(Terminal(patternString[i]))
                    i++
                }
                else -> {
                    i++
                }
            }
        }

        return result
    }

    fun applySubstitution(pattern: Pattern, substitution: Substitution): String {
        return pattern.joinToString("") { element ->
            when (element) {
                is Terminal -> element.symbol.toString()
                is Variable -> substitution[element.name] ?: "?"
            }
        }
    }
}

fun main() {
    val regularPattern = PatternParser.parsePattern("x1 x2 as a x3")
    val word = "Vasya works as a developer"

    val result = RegularPatternMatcher.matchRegularPattern(regularPattern, word)
    if (result != null) {
        println("Match found: $result")
        val applied = PatternParser.applySubstitution(regularPattern, result)
        println("Origin word: $applied")
    } else {
        println("No match found")
    }

}