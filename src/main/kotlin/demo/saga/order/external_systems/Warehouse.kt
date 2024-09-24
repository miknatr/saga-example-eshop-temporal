package demo.saga.order.external_systems

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class Warehouse {
    fun orderItems(orderId: String, items: String): WarehouseOrderResult {
        // ...
        // Here should be a call to remote warehouse API
        // ...

        val random = Random.nextInt(0, 100)

        if (random < 20) {
            return WarehouseOrderResult.OUT_OF_STOCK
        }

        if (random < 40) {
            return WarehouseOrderResult.PARTIAL
        }

        return WarehouseOrderResult.SUCCESS
    }

    fun notifyAboutFail(orderId: String) {
        // ...
        // Here should be a call to remote warehouse API
        // ...
    }
}
