package demo.saga.order.saga

import demo.saga.order.domain.Order
import demo.saga.order.domain.OrderRepository
import demo.saga.order.external_systems.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class OrderActivities : OrderActivitiesInterface {
    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var paymentProvider: PaymentProvider

    @Autowired
    private lateinit var antiFraud: AntiFraud

    @Autowired
    private lateinit var warehouse: Warehouse

    @Autowired
    private lateinit var notificationCenter: NotificationCenter

    override fun placeOrder(
        orderId: String,
        workflowId: String,
        payerName: String,
        amount: Long,
        itemsDescription: String
    ): String {
        val order = Order.constructOrder(orderId, workflowId, payerName, amount, itemsDescription)
        orderRepository.save(order)
        return order.id
    }

    override fun holdMoney(orderId: String) {
        val order = orderRepository.findById(orderId).get()

        order.startHolding()
        orderRepository.save(order)

        val transactionId = paymentProvider.hold(order.id, order.amount)

        order.markAsHeld(transactionId)
        orderRepository.save(order)
    }

    override fun cancelHold(orderId: String) {
        val order = orderRepository.findById(orderId).get()

        if (order.transactionId == null) {
            return
        }

        order.startUnholding()
        orderRepository.save(order)

        paymentProvider.cancel(order.transactionId!!)

        order.markAsUnheld()
        orderRepository.save(order)
    }

    override fun checkIsRiskyByAntiFraud(orderId: String): Boolean {
        val order = orderRepository.findById(orderId).get()

        order.startChecking()
        orderRepository.save(order)

        val riskScore = antiFraud.calculateRiskScore(order.id, order.amount)

        val isCustomerRisky = riskScore > AntiFraud.ACCEPTABLE_RISK_SCORE

        order.markAsChecked(riskScore, isCustomerRisky)
        orderRepository.save(order)

        return isCustomerRisky
    }

    override fun orderItemsFromWarehouse(orderId: String): WarehouseOrderResult {
        val order = orderRepository.findById(orderId).get()

        order.startOrdering()
        orderRepository.save(order)

        val warehouseOrderResult = warehouse.orderItems(order.id, order.itemsDescription)

        order.markAsOrdered()
        orderRepository.save(order)

        return warehouseOrderResult
    }

    override fun notifyWarehouseAboutFail(orderId: String) {
        val order = orderRepository.findById(orderId).get()

        order.startCancelingAtWarehouse()
        orderRepository.save(order)

        warehouse.notifyAboutFail(order.id)

        order.markAsCanceledAtWarehouse()
        orderRepository.save(order)
    }

    override fun captureMoney(orderId: String) {
        val order = orderRepository.findById(orderId).get()

        order.startCapturing()
        orderRepository.save(order)

        paymentProvider.capture(order.transactionId!!)

        order.markAsCaptured()
        orderRepository.save(order)
    }

    override fun notifyCustomerAboutSuccess(orderId: String) {
        val order = orderRepository.findById(orderId).get()

        order.startNotifyAboutSuccess()
        orderRepository.save(order)

        notificationCenter.notifyCustomer(order.customerName, "Your order has been successfully processed")

        order.markAsNotifiedAboutSuccess()
        orderRepository.save(order)
    }

    override fun notifyCustomerAboutFail(orderId: String) {
        val order = orderRepository.findById(orderId).get()

        order.startNotifyAboutCancel()
        orderRepository.save(order)

        notificationCenter.notifyCustomer(order.customerName, "Your order has been successfully processed")

        order.markAsNotifiedAboutCancel()
        orderRepository.save(order)
    }

    override fun markOrderAsHandledManually(orderId: String) {
        val order = orderRepository.findById(orderId).get()

        order.markAsHandledManually()

        orderRepository.save(order)
    }

    override fun markOrderAsPaidAndDispatched(orderId: String) {
        val order = orderRepository.findById(orderId).get()

        order.markAsPaidAndDispatched()

        orderRepository.save(order)
    }

    override fun cancelOrder(orderId: String, reason: String) {
        val order = orderRepository.findById(orderId).get()

        order.cancel(reason)

        orderRepository.save(order)
    }

    override fun moveFlowToManualHandling(orderId: String, reason: String) {
        val order = orderRepository.findById(orderId).get()

        order.moveToManualHandling(reason)

        orderRepository.save(order)
    }
}
