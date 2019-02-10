import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.consumes
import kotlinx.coroutines.channels.produce
import kotlin.coroutines.CoroutineContext

/**
 * http://reactivex.io/documentation/operators/distinct.html
 */
@UseExperimental(InternalCoroutinesApi::class)
fun <T> ReceiveChannel<T>.distinctUntilChanged(
    context: CoroutineContext = Dispatchers.Unconfined
): ReceiveChannel<T> =
    GlobalScope.produce(context, onCompletion = consumes()) {
        var pervius: T? = null
        consumeEach { current ->
            if (pervius != current) {
                pervius = current
                send(current)
            }
        }
    }

/**
 * http://reactivex.io/documentation/operators/debounce.html
 */
@UseExperimental(InternalCoroutinesApi::class)
fun <T> ReceiveChannel<T>.debounce(
    time: Long,
    context: CoroutineContext = Dispatchers.Unconfined
): ReceiveChannel<T> =
    GlobalScope.produce(context, onCompletion = consumes()) {
        var job: Job? = null
        consumeEach { element ->
            job?.cancel()
            job = launch {
                delay(time)
                offer(element)
            }
        }
    }

/**
 * http://reactivex.io/documentation/operators/sample.html
 */
@UseExperimental(InternalCoroutinesApi::class)
fun <T> ReceiveChannel<T>.throttleFirst(
    interval: Long,
    context: CoroutineContext = Dispatchers.Unconfined
): ReceiveChannel<T> =
    GlobalScope.produce(context, onCompletion = consumes()) {
        var nextLaunch: Long = 0
        consumeEach { element ->
            val currentTime = System.currentTimeMillis()
            if (nextLaunch <= currentTime) {
                nextLaunch = currentTime + interval
                send(element)
            }
        }
    }

/**
 * http://reactivex.io/documentation/operators/sample.html
 */
@UseExperimental(InternalCoroutinesApi::class)
fun <T> ReceiveChannel<T>.throttleLast(
    interval: Long,
    context: CoroutineContext = Dispatchers.Unconfined
): ReceiveChannel<T> =
    GlobalScope.produce(context, onCompletion = consumes()) {
        var isLaunched = false
        var current: T
        consumeEach { next ->
            current = next
            if (!isLaunched) {
                isLaunched = true
                launch {
                    delay(interval)
                    isLaunched = false
                    send(current)
                }
            }
        }
    }
