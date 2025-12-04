package util

interface BasicMatcher {
    fun match(pattern: Pattern, word: Word, substitution: MutableMap<String, String> = mutableMapOf()): Substitution?
}