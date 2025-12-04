package util

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MeasureTime

class TimeMeasurer {
    companion object {
        inline fun <T> measure(block: () -> T): T {
            val startTime = System.currentTimeMillis()
            try {
                return block()
            } finally {
                val endTime = System.currentTimeMillis()
                println("Выполнено за ${endTime - startTime}ms")
            }
        }
    }
}
