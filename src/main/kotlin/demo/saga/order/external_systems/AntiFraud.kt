package demo.saga.order.external_systems

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class AntiFraud {
    companion object {
        const val ACCEPTABLE_RISK_SCORE = 85
    }

    fun calculateRiskScore(payerName: String, amount: Long): Int {
        // ...
        // Here should be a call to remote anti-fraud system API
        // ...

        return Random.nextInt(0, 100)
    }
}
