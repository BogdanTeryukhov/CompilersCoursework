package matchers.Repv

// позволяет вычислять хеш любой подстроки за O(1) времени
class RollingHash(s: String) {
    private val mod = 1_000_000_007L // большое простое число
    private val base = 911382323L // основание полинома

    private val prefix = LongArray(s.length + 1) // prefix [i] - хеш префикса строки s длины i
    private val power = LongArray(s.length + 1)

    init {
        power[0] = 1
        for (i in s.indices) {
            prefix[i + 1] = (prefix[i] * base + s[i].code) % mod
            power[i + 1] = (power[i] * base) % mod
        }
    }

    fun getHash(l: Int, r: Int): Long {
        return (prefix[r] - prefix[l] * power[r - l] % mod + mod) % mod
    }
}
