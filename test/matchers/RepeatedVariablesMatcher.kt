package matchers.matchers

import org.junit.Test
import PatternParser.parsePattern
import junit.framework.TestCase.*
import matchers.RepeatedVariablesMatcher

class RepeatedVariablesMatcherTest {

    @Test
    fun testCalculateRepeatedVariablesCountWithNoRepeats() {
        val matcher = RepeatedVariablesMatcher(2)
        val pattern = parsePattern("x1 x2 x3")
        val count = matcher.calculateRepeatedVariablesCount(pattern)
        assertEquals(0, count)
    }

    @Test
    fun testCalculateRepeatedVariablesCountWithSingleRepeat() {
        val matcher = RepeatedVariablesMatcher(2)
        val pattern = parsePattern("x1 x1 x2")
        val count = matcher.calculateRepeatedVariablesCount(pattern)
        assertEquals(1, count)
    }

    @Test
    fun testCalculateRepeatedVariablesCountWithMultipleRepeats() {
        val matcher = RepeatedVariablesMatcher(3)
        val pattern = parsePattern("x1 x1 x2 x2 x3 x3")
        val count = matcher.calculateRepeatedVariablesCount(pattern)
        assertEquals(3, count)
    }

    @Test
    fun testCalculateRepeatedVariablesCountWithMixedRepeats() {
        val matcher = RepeatedVariablesMatcher(2)
        val pattern = parsePattern("x1 x2 x1 x3 x2 x4")
        val count = matcher.calculateRepeatedVariablesCount(pattern)
        assertEquals(2, count)
    }

    @Test
    fun testMatchSingleRepeatedVariable() {
        val matcher = RepeatedVariablesMatcher(1)
        val pattern = parsePattern("x1 x1 x1")
        val word = "a a a"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchSingleRepeatedVariableLongValue() {
        val matcher = RepeatedVariablesMatcher(1)
        val pattern = parsePattern("x1 x1")
        val word = "hello hello"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("hello", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchTwoRepeatedVariables() {
        val matcher = RepeatedVariablesMatcher(2)
        val pattern = parsePattern("x1 x1 x2 x2")
        val word = "a a b b"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchRepeatedVariablesWithTerminals() {
        val matcher = RepeatedVariablesMatcher(1)
        val pattern = parsePattern("start x1 middle x1 end")
        val word = "start ABC middle ABC end"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("ABC", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchComplexRepeatedPattern() {
        val matcher = RepeatedVariablesMatcher(2)
        val pattern = parsePattern("x1 x2 x1 x2 x3")
        val word = "a b a b c"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))
        assertEquals("c", result?.get("x3"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchNoRepeatedVariables() {
        val matcher = RepeatedVariablesMatcher(0)
        val pattern = parsePattern("x1 x2 x3")
        val word = "a b c"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))
        assertEquals("c", result?.get("x3"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testNoMatchWhenWordDoesntMatchPattern() {
        val matcher = RepeatedVariablesMatcher(1)
        val pattern = parsePattern("x1 x1 x1")
        val word = "abc"
        val result = matcher.match(pattern, word)

        assertNull(result)
    }

    @Test
    fun testMatchWithDifferentLengthRepeats() {
        val matcher = RepeatedVariablesMatcher(2)
        val pattern = parsePattern("x1 x2 x1 x2")
        val word = "hello world hello world"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("hello", result?.get("x1"))
        assertEquals("world", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchInterleavedRepeatedVariables() {
        val matcher = RepeatedVariablesMatcher(3)
        val pattern = parsePattern("x1 x2 x1 x3 x2 x3")
        val word = "a b a c b c"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))
        assertEquals("c", result?.get("x3"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchWithMaxRepeatedVarsZero() {
        val matcher = RepeatedVariablesMatcher(0)
        val pattern = parsePattern("x1 x2 x3")
        val word = "x y z"
        val result = matcher.match(pattern, word)

        assertNotNull(result)

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchSingleCharacterRepeats() {
        val matcher = RepeatedVariablesMatcher(1)
        val pattern = parsePattern("x1 x1 x1 x1 x1")
        val word = "a a a a a"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testCalculateRepeatedVariablesCountWithComplexPattern() {
        val matcher = RepeatedVariablesMatcher(5)
        val pattern = parsePattern("x1 x2 x1 x3 x2 x4 x3 x5 x4 x1")
        val count = matcher.calculateRepeatedVariablesCount(pattern)
        assertEquals(4, count)
    }
}