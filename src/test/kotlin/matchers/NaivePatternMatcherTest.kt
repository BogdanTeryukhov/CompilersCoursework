package matchers

import PatternParser.parsePattern
import org.junit.Test
import org.junit.Assert.*

class NaivePatternMatcherTest {

    private val matcher = NaivePatternMatcher()

    @Test
    fun testMatchSingleRepeatedVariable() {
        val pattern = parsePattern("x1 x1 x1")
        val word = "a a a"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchSingleVariable() {
        val pattern = parsePattern("x1")
        val word = "hello"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("hello", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchMultipleVariables() {
        val pattern = parsePattern("x1 test x2")
        val word = "this test works"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("this", result?.get("x1"))
        assertEquals("works", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchNoVariables() {
        val pattern = parsePattern("hello world")
        val word = "hello world"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertTrue(result?.isEmpty() ?: false)

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testNoMatchDifferentPattern() {
        val pattern = parsePattern("x1 x2")
        val word = "single"
        val result = matcher.match(pattern, word)

        assertNull(result)
    }

    @Test
    fun testNoMatchDifferentContent() {
        val pattern = parsePattern("x1 x1")
        val word = "a b"
        val result = matcher.match(pattern, word)

        assertNull(result)
    }

    @Test
    fun testMatchEmptyWordWithVariable() {
        val pattern = parsePattern("x1")
        val word = ""
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchComplexPattern() {
        val pattern = parsePattern("x1 and x2 or x1")
        val word = "true and false or true"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("true", result?.get("x1"))
        assertEquals("false", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }
}