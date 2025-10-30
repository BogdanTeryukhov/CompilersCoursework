package matchers.matchers

import PatternParser.parsePattern
import junit.framework.TestCase.*
import matchers.NonCrossPatternMatcher
import org.junit.Test


class NonCrossPatternMatcherTest {

    private val matcher = NonCrossPatternMatcher()

    @Test
    fun testBasicRepeatedVariables() {
        val pattern = parsePattern("x1 and x1 and x1 or x2 or x2")
        val word = "hel and hel and hel or nohel or nohel"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("hel", result?.get("x1"))
        assertEquals("nohel", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testSingleRepeatedVariable() {
        val pattern = parsePattern("x1 x1 x1")
        val word = "abc abc abc"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("abc", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testDifferentRepetitionCounts() {
        val pattern = parsePattern("start x1 x1 x1 middle x2 x2 end")
        val word = "start foo foo foo middle bar bar end"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("foo", result?.get("x1"))
        assertEquals("bar", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testVariablesWithDifferentLengths() {
        val pattern = parsePattern("x1 x1 x2 x2 x2")
        val word = "a a bbb bbb bbb"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("bbb", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testNegativeCase() {
        val pattern = parsePattern("x1 x1 x2 x2")
        val word = "hello hello world different"
        val result = matcher.match(pattern, word)
        assertNull(result)
    }
}