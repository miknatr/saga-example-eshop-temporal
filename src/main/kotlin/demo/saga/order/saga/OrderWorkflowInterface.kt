package demo.saga.order.saga

import io.temporal.workflow.SignalMethod
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface OrderWorkflowInterface {
    @WorkflowMethod
    fun startOrder(orderId: String, payerName: String, amount: Long, itemsDescription: String)

    @SignalMethod
    fun confirmPartiallyAssembledOrder(orderId: String)

    @SignalMethod
    fun cancelPartiallyAssembledOrder(orderId: String)
}
