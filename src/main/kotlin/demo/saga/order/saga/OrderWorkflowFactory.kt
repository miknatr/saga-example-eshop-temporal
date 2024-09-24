package demo.saga.order.saga

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.client.WorkflowStub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class OrderWorkflowFactory {
    @Autowired
    private lateinit var workflowClient: WorkflowClient

    private val workflowOptions: WorkflowOptions = WorkflowOptions
        .newBuilder()
        .setWorkflowId("OrderWorkflowId")
        .setTaskQueue("OrderTaskQueue")
        .build()

    fun createWorkflow(): OrderWorkflowInterface =
        workflowClient.newWorkflowStub(OrderWorkflowInterface::class.java, workflowOptions)

    fun getWorkflow(workflowId: String): WorkflowStub {
        return workflowClient.newUntypedWorkflowStub(workflowId)
    }

    fun toStub(workflow: OrderWorkflowInterface): WorkflowStub {
        return WorkflowStub.fromTyped<Any>(workflow)
    }
}
