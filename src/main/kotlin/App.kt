import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

fun main(args: Array<String>) {
    runBlocking {
        BatchTest().run()
        DebounceTest().run()
    }
}

class BatchTest {

    suspend fun run() = coroutineScope {
        val job = launch {
            produce().batch(200).consumeEach { next ->
                print(next)
                print(" ")
            }
        }
        delay(2000)
        job.cancelAndJoin()
        println()
    }

    private fun produce() = GlobalScope.produce {
        for (x in 0..100) {
            send(x)
            delay(50)
        }
    }
}

class DebounceTest {

    suspend fun run() = coroutineScope {
        val job = launch {
            val delay = 200L
            produce(delay).debounce(delay).consumeEach { next ->
                print(next)
                print(" ")
            }
        }
        delay(2000)
        job.cancelAndJoin()
        println()
    }

    private fun produce(delay: Long) = GlobalScope.produce {
        for (x in 0..100) {
            if (x % 10 == 1) delay(delay + 10)
            send(x)
        }
    }
}