package plots

import PatternParser.parsePattern
import matchers.NaivePatternMatcher
import matchers.RepeatedVariablesMatcher
import matchers.ScopeCoincidenceMatcher
import util.Pattern
import util.Variable
import java.io.File
import kotlin.system.measureTimeMillis

// Класс для измерения производительности
class PerformanceBenchmark {

    // Генерация тестовых данных
    data class TestData(
        val pattern: Pattern,
        val words: List<String>, // Слова разной длины
        val description: String
    )

    // Результаты измерений
    data class BenchmarkResult(
        val matcherName: String,
        val wordLengths: List<Int>,
        val executionTimes: List<Long>, // в миллисекундах
        val patternDescription: String
    )

    // Функция для генерации случайных слов
    private fun generateRandomWord(length: Int, alphabet: String = "abcdefghijklmnopqrstuvwxyz "): String {
        return (1..length).map { alphabet.random() }.joinToString("")
    }

    // Функция для генерации шаблонов
    private fun generatePatterns(): List<TestData> {
        return listOf(
            // 1. Простой шаблон с 1 повторяющейся переменной
            TestData(
                pattern = parsePattern("x1 x2 x1"),
                words = (10..50 step 10).map { generateRandomWord(it) },
                description = "x1 x2 x1 (repv=1, scd=2)"
            ),

            // 2. Шаблон с 2 повторяющимися переменными
            TestData(
                pattern = parsePattern("x1 x2 x1 x2"),
                words = (10..50 step 10).map { generateRandomWord(it) },
                description = "x1 x2 x1 x2 (repv=2, scd=2)"
            ),

            // 3. Сложный шаблон с пересечениями
            TestData(
                pattern = parsePattern("x1 x2 x3 x1 x2"),
                words = (10..50 step 10).map { generateRandomWord(it) },
                description = "x1 x2 x3 x1 x2 (repv=2, scd=3)"
            ),

            // 4. Регулярный шаблон (без повторений)
            TestData(
                pattern = parsePattern("x1 x2 x3 x4 x5"),
                words = (10..50 step 10).map { generateRandomWord(it) },
                description = "x1 x2 x3 x4 x5 (repv=0, scd=1)"
            )
        )
    }

    // Функция для запуска бенчмарков
    fun runBenchmarks(): List<BenchmarkResult> {
        val testPatterns = generatePatterns()
        val results = mutableListOf<BenchmarkResult>()

        // Создаем матчеры с разными параметрами
        val matchers = listOf(
            "NaiveMatcher" to NaivePatternMatcher(),
            "Repv1Matcher" to RepeatedVariablesMatcher(1),
            "Repv2Matcher" to RepeatedVariablesMatcher(2),
            "Repv3Matcher" to RepeatedVariablesMatcher(3),
            "SCD1Matcher" to ScopeCoincidenceMatcher(1),
            "SCD2Matcher" to ScopeCoincidenceMatcher(2),
            "SCD3Matcher" to ScopeCoincidenceMatcher(3)
        )

        for ((matcherName, matcher) in matchers) {
            for (testData in testPatterns) {
                try {
                    // Проверяем, подходит ли матчер для данного шаблона
                    if (isMatcherApplicable(matcher, testData.pattern, matcherName)) {
                        val executionTimes = mutableListOf<Long>()

                        for (word in testData.words) {
                            val time = measureTimeMillis {
                                matcher.match(testData.pattern, word)
                            }
                            executionTimes.add(time)
                        }

                        results.add(
                            BenchmarkResult(
                                matcherName = matcherName,
                                wordLengths = (10..50 step 10).toList(),
                                executionTimes = executionTimes,
                                patternDescription = testData.description
                            )
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    // Матчер не подходит для этого шаблона - пропускаем
                    println("Матчер $matcherName не подходит для шаблона: ${testData.description}")
                }
            }
        }

        return results
    }

    // Проверка применимости матчера
    private fun isMatcherApplicable(matcher: Any, pattern: Pattern, matcherName: String): Boolean {
        return when (matcher) {
            is RepeatedVariablesMatcher -> {
                val repeatedCount = calculateRepeatedVariablesCount(pattern)
                repeatedCount <= when (matcherName) {
                    "Repv1Matcher" -> 1
                    "Repv2Matcher" -> 2
                    "Repv3Matcher" -> 3
                    else -> 3
                }
            }
            is ScopeCoincidenceMatcher -> {
                val scd = calculateSCD(pattern)
                scd <= when (matcherName) {
                    "SCD1Matcher" -> 1
                    "SCD2Matcher" -> 2
                    "SCD3Matcher" -> 3
                    else -> 3
                }
            }
            else -> true // Наивный матчер подходит для всех
        }
    }

    // Вспомогательные функции для расчета параметров
    private fun calculateRepeatedVariablesCount(pattern: Pattern): Int {
        val counts = mutableMapOf<String, Int>()
        pattern.forEach { element ->
            if (element is Variable) {
                counts[element.name] = counts.getOrDefault(element.name, 0) + 1
            }
        }
        return counts.values.count { it > 1 }
    }

    private fun calculateSCD(pattern: Pattern): Int {
        val variableRanges = mutableMapOf<String, IntRange>()
        pattern.forEachIndexed { index, element ->
            if (element is Variable) {
                val current = variableRanges[element.name]
                if (current == null) {
                    variableRanges[element.name] = index..index
                } else {
                    variableRanges[element.name] = current.first..index
                }
            }
        }

        var maxSCD = 0
        for (i in pattern.indices) {
            var count = 0
            for (range in variableRanges.values) {
                if (i in range) count++
            }
            maxSCD = maxOf(maxSCD, count)
        }
        return maxSCD
    }

    // Генерация графика в формате gnuplot
    fun generateGnuplotScript(results: List<BenchmarkResult>, filename: String = "plot_script.gp") {
        val file = File(filename)
        val script = StringBuilder()

        script.append("""
            set terminal pngcairo size 1200,800 enhanced font 'Verdana,10'
            set output 'benchmark_results.png'
            
            set title 'Сравнение производительности матчеров шаблонов'
            set xlabel 'Длина слова (символы)'
            set ylabel 'Время выполнения (мс)'
            set grid
            
            set key outside right top
            
            set style line 1 lc rgb '#FF0000' lt 1 lw 2 pt 7 ps 1.5
            set style line 2 lc rgb '#00FF00' lt 1 lw 2 pt 7 ps 1.5
            set style line 3 lc rgb '#0000FF' lt 1 lw 2 pt 7 ps 1.5
            set style line 4 lc rgb '#FF00FF' lt 1 lw 2 pt 7 ps 1.5
            set style line 5 lc rgb '#00FFFF' lt 1 lw 2 pt 7 ps 1.5
            set style line 6 lc rgb '#FFFF00' lt 1 lw 2 pt 7 ps 1.5
            set style line 7 lc rgb '#FFA500' lt 1 lw 2 pt 7 ps 1.5
            
        """.trimIndent())

        // Группируем результаты по шаблонам
        val patterns = results.map { it.patternDescription }.distinct()

        for ((patternIndex, pattern) in patterns.withIndex()) {
            val patternResults = results.filter { it.patternDescription == pattern }

            script.append("\n# График для шаблона: $pattern\n")
            script.append("set output '${pattern.replace(" ", "_")}_results.png'\n")
            script.append("set title 'Производительность для шаблона: $pattern'\n\n")

            script.append("plot ")

            for ((i, result) in patternResults.withIndex()) {
                val dataFile = "${result.matcherName}_${pattern.replace(" ", "_")}.dat"
                val lineStyle = i + 1

                // Сохраняем данные в отдельный файл
                val dataContent = StringBuilder()
                for (j in result.wordLengths.indices) {
                    dataContent.append("${result.wordLengths[j]} ${result.executionTimes[j]}\n")
                }
                File(dataFile).writeText(dataContent.toString())

                script.append("'$dataFile' with linespoints ls $lineStyle title '${result.matcherName}'")
                if (i < patternResults.size - 1) script.append(", \\\n     ")
            }
            script.append("\n\n")
        }

        file.writeText(script.toString())
        println("Скрипт gnuplot сохранен в $filename")
        println("Для генерации графиков выполните: gnuplot $filename")
    }

    // Упрощенная версия для быстрого анализа
    fun quickAnalysis() {
        println("=== БЫСТРЫЙ АНАЛИЗ ПРОИЗВОДИТЕЛЬНОСТИ ===\n")

        val testPattern = parsePattern("x1 x2 x1")
        val wordLengths = listOf(10, 20, 30, 40, 50)

        val matchers = listOf(
            "Naive" to NaivePatternMatcher(),
            "Repv1" to RepeatedVariablesMatcher(1),
            "SCD2" to ScopeCoincidenceMatcher(2)
        )

        for ((name, matcher) in matchers) {
            print("$name: ")
            val times = mutableListOf<Long>()

            for (length in wordLengths) {
                val word = generateRandomWord(length)
                val time = measureTimeMillis {
                    matcher.match(testPattern, word)
                }
                times.add(time)
                print("${length}ch=${time}ms ")
            }

            // Анализ сложности
            val avgTime = times.average()
            println("\n   Среднее время: ${String.format("%.2f", avgTime)}ms")

            if (times.size >= 2) {
                val ratio = times.last().toDouble() / times.first()
                println("   Коэффициент роста: ${String.format("%.2f", ratio)}x")

                // Оценка сложности
                when {
                    ratio < 2 -> println("   Предполагаемая сложность: O(n)")
                    ratio < 4 -> println("   Предполагаемая сложность: O(n log n)")
                    ratio < 10 -> println("   Предполагаемая сложность: O(n²)")
                    ratio < 50 -> println("   Предполагаемая сложность: O(n³)")
                    else -> println("   Предполагаемая сложность: экспоненциальная")
                }
            }
            println()
        }
    }
}

// Основная функция для запуска бенчмарков
fun main() {
    val benchmark = PerformanceBenchmark()

    println("Запуск бенчмарков производительности матчеров...")
    println("Длина слов: от 10 до 200 символов с шагом 10\n")

    // Быстрый анализ для примера
    benchmark.quickAnalysis()

    // Полный бенчмарк (может занять время)
    println("\nЗапуск полного бенчмарка...")
    val results = benchmark.runBenchmarks()

    benchmark.generateGnuplotScript(results)

    // Вывод сводки
    println("\n=== СВОДКА РЕЗУЛЬТАТОВ ===")
    for (result in results.take(5)) { // Покажем первые 5 результатов
        println("\n${result.matcherName} - ${result.patternDescription}:")
        println("  Минимальное время: ${result.executionTimes.min()}ms")
        println("  Максимальное время: ${result.executionTimes.max()}ms")
        println("  Среднее время: ${String.format("%.2f", result.executionTimes.average())}ms")
    }
}