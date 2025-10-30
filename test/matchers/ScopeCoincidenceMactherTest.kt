package matchers.matchers

import org.junit.Test
import PatternParser.parsePattern
import junit.framework.TestCase.*
import matchers.ScopeCoincidenceMatcher

class ScopeCoincidenceMatcherTest {

    @Test
    fun testCalculateSCDWithSingleVariable() {
        val matcher = ScopeCoincidenceMatcher(1)
        val pattern = parsePattern("x1 x1 x1")
        val scd = matcher.calculateSCD(pattern)
        assertEquals(1, scd)
    }

    @Test
    fun testCalculateSCDWithNonOverlappingVariables() {
        val matcher = ScopeCoincidenceMatcher(2)
        val pattern = parsePattern("x1 x1 x1 x2 x2")
        val scd = matcher.calculateSCD(pattern)
        assertEquals(1, scd)
    }

    @Test
    fun testCalculateSCDWithOverlappingVariables() {
        val matcher = ScopeCoincidenceMatcher(3)
        val pattern = parsePattern("x1 x2 x1 x2 x1")
        val scd = matcher.calculateSCD(pattern)
        assertEquals(2, scd)
    }

    @Test
    fun testMatchSimplePatternWithSCD1() {
        val matcher = ScopeCoincidenceMatcher(1)
        val pattern = parsePattern("x1x1x1")
        val word = "aaa"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchPatternWithDifferentVariableValues() {
        val matcher = ScopeCoincidenceMatcher(1)
        val pattern = parsePattern("x1x1x2x2")
        val word = "aabb"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchPatternWithTerminals() {
        val matcher = ScopeCoincidenceMatcher(1)
        val pattern = parsePattern("x1 test x1")
        val word = "hello test hello"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("hello", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchPatternWithSCD2() {
        val matcher = ScopeCoincidenceMatcher(2)
        val pattern = parsePattern("x1 x2 x1 x2")
        val word = "a b a b"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchComplexPatternWithSCD2() {
        val matcher = ScopeCoincidenceMatcher(2)
        val pattern = parsePattern("x1 x2 x1 x2 x3 x3")
        val word = "a b a b c c"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))
        assertEquals("c", result?.get("x3"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testNoMatchWhenWordDoesntMatch() {
        val matcher = ScopeCoincidenceMatcher(1)
        val pattern = parsePattern("x1 x1 x1")
        val word = "abc"
        val result = matcher.match(pattern, word)

        assertNull(result)
    }

    @Test
    fun testMatchWithEmptySubstitutionParameter() {
        val matcher = ScopeCoincidenceMatcher(1)
        val pattern = parsePattern("x1 x1")
        val word = "a a"
        val result = matcher.match(pattern, word, mutableMapOf())

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
    }

    @Test
    fun testMatchWithPartialSubstitution() {
        val matcher = ScopeCoincidenceMatcher(2)
        val pattern = parsePattern("x1 x2 x1 x2")
        val word = "a b a b"
        val partialSubstitution = mutableMapOf("x1" to "a")
        val result = matcher.match(pattern, word, partialSubstitution)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))
    }

    @Test
    fun testMatchLongerVariableValues() {
        val matcher = ScopeCoincidenceMatcher(1)
        val pattern = parsePattern("x1 x1 x1")
        val word = "hello hello hello"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("hello", result?.get("x1"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testMatchMixedPattern() {
        val matcher = ScopeCoincidenceMatcher(2)
        val pattern = parsePattern("start x1 middle x2 end x1")
        val word = "start ABC middle DEF end ABC"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("ABC", result?.get("x1"))
        assertEquals("DEF", result?.get("x2"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }

    @Test
    fun testCalculateSCDWithComplexOverlap() {
        val matcher = ScopeCoincidenceMatcher(3)
        val pattern = parsePattern("x1 x2 x3 x1 x2 x1")
        val scd = matcher.calculateSCD(pattern)
        assertEquals(3, scd)
    }

    @Test
    fun testNoMatchWithInconsistentPartialSubstitution() {
        val matcher = ScopeCoincidenceMatcher(2)
        val pattern = parsePattern("x1 x2 x1 x2")
        val word = "abab"
        val invalidSubstitution = mutableMapOf("x1" to "wrong")
        val result = matcher.match(pattern, word, invalidSubstitution)

        assertNull(result)
    }

    @Test
    fun testMatchWithMaxSCD3() {
        val matcher = ScopeCoincidenceMatcher(3)
        val pattern = parsePattern("x1 x2 x3 x1 x2 x3")
        val word = "a b c a b c"
        val result = matcher.match(pattern, word)

        assertNotNull(result)
        assertEquals("a", result?.get("x1"))
        assertEquals("b", result?.get("x2"))
        assertEquals("c", result?.get("x3"))

        val applied = PatternParser.applySubstitution(pattern, result!!)
        assertEquals(word, applied)
    }
}