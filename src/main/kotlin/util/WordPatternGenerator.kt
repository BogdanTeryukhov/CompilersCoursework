package util

interface WordPatternGenerator {
    fun generateWordAndPattern(numOfVars: Int, alphabet: String = "abcdefghijklmnopqrstuvwxyz"): Pair<String, String>
}