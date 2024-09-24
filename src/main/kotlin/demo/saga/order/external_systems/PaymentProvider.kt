package demo.saga.order.external_systems

import org.springframework.stereotype.Component
import kotlin.math.absoluteValue
import kotlin.random.Random

@Component
class PaymentProvider {
    fun hold(payerName: String, amount: Long): String {
        // ...
        // Here should be a call to remote payment provider API
        // ...

        return Random.nextLong().absoluteValue.toString()
    }

    fun capture(transactionId: String) {
        // ...
        // Here should be a call to remote payment provider API
        // ...
    }

    fun cancel(transactionId: String) {
        // ...
        // Here should be a call to remote payment provider API
        // ...
    }
}
