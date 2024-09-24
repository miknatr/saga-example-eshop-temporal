package demo.saga.order.saga

import demo.saga.order.external_systems.WarehouseOrderResult
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

class OrderWorkflow : OrderWorkflowInterface {
    private val steps: OrderActivitiesInterface =
        Workflow.newActivityStub(
            OrderActivitiesInterface::class.java,
            ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(60))
                .build()
        )

    private var isManualConfirmed = false

    override fun processOrder(payerName: String, amount: Long, itemsDescription: String): String {
        val workflowId = Workflow.getInfo().runId

        val orderId = steps.placeOrder(workflowId, payerName, amount, itemsDescription)

        try {
            steps.holdMoney(orderId)

            val isRisky = steps.checkIsRiskyByAntiFraud(orderId)

            if (isRisky) {
                steps.cancelHold(orderId)
                steps.notifyCustomerAboutFail(orderId)
                val finalState = steps.cancelOrder(orderId, "Payments from this customer are too risky")
                return finalState
            }

            val warehouseOrderResult = steps.orderItemsFromWarehouse(orderId)

            if (warehouseOrderResult == WarehouseOrderResult.OUT_OF_STOCK) {
                // Consider to order from another warehouse
                steps.cancelHold(orderId)
                steps.notifyCustomerAboutFail(orderId)
                val finalState = steps.cancelOrder(orderId, "Ordered items are out of stock")
                return finalState
            }

            if (warehouseOrderResult == WarehouseOrderResult.PARTIAL) {
                val finalState = steps.moveFlowToManualHandling(orderId, "Only some items are in stock")
                // Workflow.await { isManualConfirmed }
                return finalState
            }

            steps.captureMoney(orderId)
            val finalState = steps.markOrderAsPaidAndDispatched(orderId)

            return finalState
        } catch (e: Exception) {
            steps.cancelHold(orderId)
            steps.notifyWarehouseAboutFail(orderId)
            steps.notifyCustomerAboutFail(orderId)

            val cancelReason = e.message ?: "Unknown error"

            val finalState = steps.cancelOrder(orderId, cancelReason)

            return finalState
        }
    }

    override fun confirmOrderManually(orderId: String) {
        isManualConfirmed = true
    }
}
