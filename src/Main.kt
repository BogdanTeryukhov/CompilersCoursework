import PatternParser.parsePattern
import NonCrossPatternMatcher

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

    // регулярный шаблон
    val regularPattern = parsePattern("x1 x2 as a x3")
    val word1 = "Vasya works as a developer"
    val result1 = RegularPatternMatcher.matchRegularPattern(regularPattern, word1)
    if (result1 != null) {
        println("Match found: $result1")
        val applied = PatternParser.applySubstitution(regularPattern, result1)
        println("Origin word: $applied")
    } else {
        println("No match found")
    }

    // непересекающийся шаблон
    val nonCrossPatternMatcher = NonCrossPatternMatcher()
    val nonCrossPattern = parsePattern("x1 and x1 and x1 or x2 or x2")
    val word2 = "hel and hel and hel or nohel or nohel"

    val result2= nonCrossPatternMatcher.match(nonCrossPattern, word2)
    if (result2 != null) {
        println("Match found: $result2")
        val applied = PatternParser.applySubstitution(nonCrossPattern, result2)
        println("Origin word: $applied")
    } else {
        println("No match found")
    }
}