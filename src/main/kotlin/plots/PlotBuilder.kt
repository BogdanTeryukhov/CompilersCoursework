import PatternParser.parsePattern
import matchers.NaivePatternMatcher
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.groupBy
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import plots.generateWordAndPattern
import util.BasicMatcher
import util.TimeMeasurer

private val naiveMatcher = NaivePatternMatcher()

fun countedValues(numOfVars: Int, matcher: BasicMatcher): Long {
    val (word, pattern) = generateWordAndPattern(numOfVars)
    val elapsedTime = TimeMeasurer.measure {
        matcher.match(pattern = parsePattern(pattern), word = word)
    }
    println("TIME: ${elapsedTime}ms")
    return elapsedTime
}

fun main() {
    val symbolLengths = listOf(10, 20, 30, 40, 50, 60)

    val naiveMatcherResults = mutableListOf<Long>()

    for (symbolLength in symbolLengths) {
        val value  = countedValues(symbolLength / 10, naiveMatcher)
        naiveMatcherResults.add(value)
    }


    val dataset = dataFrameOf(
        "symbolLengths" to symbolLengths,
        "time" to naiveMatcherResults,
        "category" to List(6) { "Naive" }
    )

    dataset.groupBy("category").plot {
        line {
            x("symbolLengths")
            y("time")
            color("category")
        }
    }.save("Naive_results.png")
}