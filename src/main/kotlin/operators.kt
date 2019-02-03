import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.consumes
import kotlinx.coroutines.channels.produce

@UseExperimental(InternalCoroutinesApi::class)
fun <T> ReceiveChannel<T>.debounce(time: Long): ReceiveChannel<T> = GlobalScope.produce(onCompletion = consumes()) {
    var job: Job? = null
    consumeEach { next ->
        job?.cancel()
        job = launch {
            delay(time)
            offer(next)
        }
    }
}

@UseExperimental(InternalCoroutinesApi::class)
fun <T> ReceiveChannel<T>.batch(time: Long): ReceiveChannel<T> = GlobalScope.produce(onCompletion = consumes()) {
    var isLaunched = false
    var current: T
    consumeEach { next ->
        current = next
        if (!isLaunched) {
            isLaunched = true
            launch {
                delay(time)
                isLaunched = false
                send(current)
            }
        }
    }
}