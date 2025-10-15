sealed class PatternElement
data class Terminal(val symbol: Char) : PatternElement()
data class Variable(val name: String) : PatternElement()

typealias Pattern = List<PatternElement>
typealias Substitution = Map<String, String>

// Слово
typealias Word = String