package demo.saga.order.saga

import demo.saga.order.external_systems.WarehouseOrderResult
import io.temporal.activity.ActivityInterface

@ActivityInterface
interface OrderActivitiesInterface {
    fun placeOrder(
        orderId: String,
        workflowId: String,
        payerName: String,
        amount: Long,
        itemsDescription: String
    ): String

    fun holdMoney(orderId: String)

    fun cancelHold(orderId: String)

    fun checkIsRiskyByAntiFraud(orderId: String): Boolean

    fun orderItemsFromWarehouse(orderId: String): WarehouseOrderResult

    fun notifyWarehouseAboutFail(orderId: String)

    fun captureMoney(orderId: String)

    fun notifyCustomerAboutSuccess(orderId: String)

    fun notifyCustomerAboutFail(orderId: String)

    fun markOrderAsHandledManually(orderId: String)

    fun markOrderAsPaidAndDispatched(orderId: String)

    fun cancelOrder(orderId: String, reason: String)

    fun moveFlowToManualHandling(orderId: String, reason: String)
}
