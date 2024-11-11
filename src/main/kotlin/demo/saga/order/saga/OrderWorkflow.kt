package demo.saga.order.saga

import demo.saga.order.external_systems.WarehouseOrderResult
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.CompletablePromise
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

    private val confirmationPromise: CompletablePromise<Void> = Workflow.newPromise()
    private val cancellationPromise: CompletablePromise<Void> = Workflow.newPromise()

    override fun startOrder(orderId: String, payerName: String, amount: Long, itemsDescription: String) {
        val workflowId = Workflow.getInfo().workflowId

        steps.placeOrder(orderId, workflowId, payerName, amount, itemsDescription)

        try {
            steps.holdMoney(orderId)

            val isRisky = steps.checkIsRiskyByAntiFraud(orderId)

            if (isRisky) {
                steps.cancelHold(orderId)
                steps.notifyCustomerAboutFail(orderId)
                steps.cancelOrder(orderId, "Payments from this customer are too risky")
                return
            }

            val warehouseOrderResult = steps.orderItemsFromWarehouse(orderId)

            if (warehouseOrderResult == WarehouseOrderResult.OUT_OF_STOCK) {
                // Consider to order from another warehouse
                steps.cancelHold(orderId)
                steps.notifyCustomerAboutFail(orderId)
                steps.cancelOrder(orderId, "Ordered items are out of stock")
                return
            }

            if (warehouseOrderResult == WarehouseOrderResult.PARTIAL) {
                steps.moveFlowToManualHandling(orderId, "Only some items are in stock")

                Workflow.await { confirmationPromise.isCompleted || cancellationPromise.isCompleted }

                steps.markOrderAsHandledManually(orderId)

                if (cancellationPromise.isCompleted) {
                    steps.cancelOrder(orderId, "Order is manually cancelled")
                    return
                }
            }

            steps.captureMoney(orderId)
            steps.markOrderAsPaidAndDispatched(orderId)
            return
        } catch (e: Exception) {
            steps.cancelHold(orderId)
            steps.notifyWarehouseAboutFail(orderId)
            steps.notifyCustomerAboutFail(orderId)

            val cancelReason = e.message ?: "Unknown error"

            steps.cancelOrder(orderId, cancelReason)
        }
    }

    override fun confirmPartiallyAssembledOrder(orderId: String) {
        confirmationPromise.complete(null)
    }

    override fun cancelPartiallyAssembledOrder(orderId: String) {
        cancellationPromise.complete(null)
    }
}
