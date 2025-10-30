package matchers.matchers

import PatternParser.parsePattern
import junit.framework.TestCase.*
import matchers.RegularPatternMatcher
import org.junit.Test

class RegularPatternMatcherTest {

    private val matcher = RegularPatternMatcher()

    @Test
    fun testIsRegularWithRegularPattern() {
        val pattern = parsePattern("x1 test x2 or x3")
        assertTrue(matcher.isRegular(pattern))
    }

    @Test
    fun testIsRegularWithNonRegularPattern() {
        val pattern = parsePattern("x1 test x1 or x2")
        assertFalse(matcher.isRegular(pattern))
    }

    @Test
    fun testMatchSimplePattern() {
        val pattern = parsePattern("x1 test x2")
        val word = "hello test world"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("hello", result?.get("x1"))
        assertEquals("world", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchPatternWithTerminalsOnly() {
        val pattern = parsePattern("hello world")
        val word = "hello world"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertTrue(result?.isEmpty() == true)

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchPatternWithVariablesOnly() {
        val pattern = parsePattern("x1 x2 x3")
        val word = "a bb ccc"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("bb", result?.get("x2"))
        assertEquals("ccc", result?.get("x3"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchPatternWithVariableAtStart() {
        val pattern = parsePattern("x1 end")
        val word = "variablepart end"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("variablepart", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchPatternWithVariableAtEnd() {
        val pattern = parsePattern("start x1")
        val word = "start variablepart"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("variablepart", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchPatternWithMultipleVariables() {
        val pattern = parsePattern("begin x1 middle x2 end")
        val word = "begin first middle second end"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("first", result?.get("x1"))
        assertEquals("second", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testNoMatchWhenWordTooShort() {
        val pattern = parsePattern("x1 test x2")
        val word = "hello test"
        val result = matcher.match(pattern, word)

        assertNull(result)
    }

    @Test
    fun testNoMatchWhenTerminalsDontMatch() {
        val pattern = parsePattern("x1 test x2")
        val word = "hello world x2"
        val result = matcher.match(pattern, word)

        assertNull(result)
    }

    @Test
    fun testNoMatchWhenWordTooLong() {
        val pattern = parsePattern("x1 test")
        val word = "hello test extra"
        val result = matcher.match(pattern, word)

        assertNull(result)
    }

    @Test
    fun testMatchWithEmptyVariableValue() {
        val pattern = parsePattern("x1x2")
        val word = "ab"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))
    }

    @Test
    fun testComplexRegularPattern() {
        val pattern = parsePattern("a x1 b x2 c x3 d x4 e")
        val word = "a first b second c third d fourth e"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("first", result?.get("x1"))
        assertEquals("second", result?.get("x2"))
        assertEquals("third", result?.get("x3"))
        assertEquals("fourth", result?.get("x4"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }
}