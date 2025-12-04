package plots

val VARIABLES_LIST = listOf("x1 ", "x2 ", "x3 ")
val WORD_PREFIX = listOf("TFL1 ", "TFL2 ", "TFL3 ")

fun generateWordAndPattern(numOfVars: Int, alphabet: String = "abcdefghijklmnopqrstuvwxyz"): Pair<String, String> {
    val subword = (1..6).map { alphabet.random() }.joinToString("")

    val pattern: StringBuilder = StringBuilder()
    val word: StringBuilder = StringBuilder()

    for (i in 1..numOfVars) {
        val randomIndex = listOf(0, 1, 2).random()
        pattern.append(VARIABLES_LIST[randomIndex] + subword + " ")
        word.append(WORD_PREFIX[randomIndex] + subword + " ")
    }
    return (word.toString() to pattern.toString())
}