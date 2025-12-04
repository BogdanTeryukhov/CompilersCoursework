import util.Pattern
import util.PatternElement
import util.Substitution
import util.Terminal
import util.Variable

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