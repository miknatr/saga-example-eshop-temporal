package demo.saga

import demo.saga.order.saga.OrderActivities
import demo.saga.order.saga.OrderWorkflow
import io.temporal.client.WorkflowClient
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TemporalConfig {
    @Bean
    fun createService(): WorkflowServiceStubs = WorkflowServiceStubs.newLocalServiceStubs()

    @Bean
    fun createClient(@Autowired service: WorkflowServiceStubs): WorkflowClient = WorkflowClient.newInstance(service)

    @Bean
    fun createWorkerFactory(
        @Autowired orderActivities: OrderActivities,
        @Autowired client: WorkflowClient
    ): WorkerFactory {
        val workerFactory = WorkerFactory.newInstance(client)

        workerFactory.newWorker("OrderTaskQueue").apply {
            registerWorkflowImplementationTypes(OrderWorkflow::class.java)
            registerActivitiesImplementations(orderActivities)
        }

        return workerFactory
    }
}
