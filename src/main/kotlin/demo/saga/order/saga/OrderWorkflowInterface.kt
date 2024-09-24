package demo.saga.order.saga

import io.temporal.workflow.SignalMethod
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface OrderWorkflowInterface {
    @WorkflowMethod
    fun processOrder(payerName: String, amount: Long, itemsDescription: String): String

    @SignalMethod
    fun confirmOrderManually(orderId: String)
}
