import PatternParser.parsePattern
import matchers.NaivePatternMatcher
import matchers.NonCrossPatternMatcher
import matchers.RegularPatternMatcher
import matchers.RepeatedVariablesMatcher
import matchers.ScopeCoincidenceMatcher
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.groupBy
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import util.BasicMatcher
import util.TimeMeasurer
import util.WordPatternGenerator

private val naiveMatcher = NaivePatternMatcher()
private val regularMatcher = RegularPatternMatcher()
private val repeatedVariablesMatcher = RepeatedVariablesMatcher(maxRepeatedVars = 3)
private val nonCrossPatternMatcher = NonCrossPatternMatcher()
private val scopeCoincidenceMatcher = ScopeCoincidenceMatcher(maxSCD = 6)

private val symbolLengths = listOf(10, 20, 30)

fun buildPlots(symbolLengths: List<Int>, naiveMatcherResults: List<Long>, otherMatcherResults: List<Long>, matcherName: String) {
    val dataset = dataFrameOf(
        "symbolLengths" to symbolLengths + symbolLengths,
        "time" to naiveMatcherResults + otherMatcherResults,
        "category" to List(3) { "Naive" } + List(3) { matcherName }
    )

    dataset.groupBy("category").plot {
        line {
            x("symbolLengths")
            y("time")
            color("category")
        }
    }.save("Naive_${matcherName}_results.png")
}

fun countedValues(numOfVars: Int, matcher: BasicMatcher, generator: WordPatternGenerator): Pair<Long, Long> {
    val (word, pattern) = generator.generateWordAndPattern(numOfVars)
    val parsedPattern = parsePattern(pattern)
    val elapsedTime = TimeMeasurer.measure {
        matcher.match(pattern = parsedPattern, word = word)
    }
    val elapsedTimeNaiveMatcher = TimeMeasurer.measure {
        naiveMatcher.match(pattern = parsedPattern, word = word)
    }
    println("NAIVE TIME: $elapsedTimeNaiveMatcher ms")
    println("${matcher.javaClass.simpleName} TIME: $elapsedTime ms")
    return elapsedTime to elapsedTimeNaiveMatcher
}

fun prePlotManipulations(matcher: BasicMatcher, generator: WordPatternGenerator, matcherName: String) {
    val naiveMatcherResults = mutableListOf<Long>()
    val otherMatcherResults = mutableListOf<Long>()

    for (symbolLength in symbolLengths) {
        val (value, naiveValue) = countedValues(symbolLength / 10, matcher, generator)
        otherMatcherResults.add(value)
        naiveMatcherResults.add(naiveValue)
    }

    buildPlots(symbolLengths, naiveMatcherResults, otherMatcherResults, matcherName)
}

fun main() {
    prePlotManipulations(scopeCoincidenceMatcher, scopeCoincidenceMatcher, "ScopeCoincidence")
    prePlotManipulations(regularMatcher, regularMatcher, "Regular")
    prePlotManipulations(nonCrossPatternMatcher, nonCrossPatternMatcher, "NonCross")
    prePlotManipulations(repeatedVariablesMatcher, repeatedVariablesMatcher, "RepeatedVariables")
}