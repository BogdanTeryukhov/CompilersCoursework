import PatternParser.parsePattern
import matchers.NaivePatternMatcher
import matchers.NonCrossPatternMatcher
import matchers.RegularPatternMatcher
import matchers.Repv.RepeatedVariablesMatcher
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
private val repeatedVariablesMatcher = RepeatedVariablesMatcher(maxRepeatedVars = 100000)
private val nonCrossPatternMatcher = NonCrossPatternMatcher()
private val scopeCoincidenceMatcher = ScopeCoincidenceMatcher(maxSCD = 4)


private val maxSymbols = 10000

fun buildPlots(symbolLengths: List<Int>, naiveMatcherResults: List<Long>, otherMatcherResults: List<Long>, matcherName: String) {
    val dataset = dataFrameOf(
        "symbolLengths" to symbolLengths + symbolLengths,
        "time" to naiveMatcherResults + otherMatcherResults,
        "category" to List(maxSymbols / 20) { "Naive" } + List(maxSymbols / 20) { matcherName }
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

    for (symbolLength in 20..maxSymbols step 20) {
        val (value, naiveValue) = countedValues(symbolLength / 10, matcher, generator)
        otherMatcherResults.add(value)
        naiveMatcherResults.add(naiveValue)
    }

    buildPlots((20..maxSymbols step 20).toList(), naiveMatcherResults, otherMatcherResults, matcherName)
}

fun main() {
    prePlotManipulations(regularMatcher, regularMatcher, "Regular")
    prePlotManipulations(scopeCoincidenceMatcher, scopeCoincidenceMatcher, "ScopeCoincidence")
    prePlotManipulations(nonCrossPatternMatcher, nonCrossPatternMatcher, "NonCross")
    prePlotManipulations(repeatedVariablesMatcher, repeatedVariablesMatcher, "RepeatedVariables")
}