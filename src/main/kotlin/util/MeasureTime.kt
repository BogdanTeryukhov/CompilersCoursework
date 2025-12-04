package util

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MeasureTime

class TimeMeasurer {
    companion object {
        inline fun <T> measure(block: () -> T): Long {
            val startTime = System.currentTimeMillis()
            try {
                block()
            } finally {
                val endTime = System.currentTimeMillis()
                return endTime - startTime
            }
        }
    }
}
