import matchers.NaivePatternMatcherTest
import matchers.matchers.*
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    NonCrossPatternMatcherTest::class,
    RegularPatternMatcherTest::class,
    ScopeCoincidenceMatcherTest::class,
    RepeatedVariablesMatcherTest::class,
    NaivePatternMatcherTest::class,
)
class AllMatchersTestSuite