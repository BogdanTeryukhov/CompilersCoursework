package matchers.Repv

class RollingHash(s: String) {
    private val mod = 1_000_000_007L
    private val base = 911382323L

    private val prefix = LongArray(s.length + 1)
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
