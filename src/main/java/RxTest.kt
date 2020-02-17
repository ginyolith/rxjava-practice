import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

fun main() {
    val flowable = Flowable.create(object : FlowableOnSubscribe<String> {
        override fun subscribe(emitter: FlowableEmitter<String>) {
            val datas = listOf("Hello world", "こんにちは世界")
            datas.forEach {
                if (emitter.isCancelled) return

                emitter.onNext(it)
            }

            emitter.onComplete()
        }
    }, BackpressureStrategy.BUFFER) // 超過したデータはバッファする

    flowable
        .observeOn(Schedulers.computation())
        .subscribe(object : Subscriber<String> {
            private var subscription: Subscription? = null

            override fun onSubscribe(s: Subscription?) {
                this.subscription = s

                this.subscription?.request(1)
            }

            override fun onNext(t: String?) {
                val thread = Thread.currentThread().name
                println("$thread : $t")
                subscription?.request(1)
            }

            override fun onComplete() {
                val thread = Thread.currentThread().name
                println("$thread : complete")
            }

            override fun onError(t: Throwable?) {
                val thread = Thread.currentThread().name
                println(thread)
                t?.printStackTrace()
            }
        })

    Thread.sleep(500)
}