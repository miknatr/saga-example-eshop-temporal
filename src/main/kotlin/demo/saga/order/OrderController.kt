package demo.saga.order

import demo.saga.order.domain.Order
import demo.saga.order.domain.OrderRepository
import demo.saga.order.domain.OrderStatus
import demo.saga.order.saga.OrderWorkflowInterface
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import kotlin.math.absoluteValue
import kotlin.random.Random

@RestController
class OrderController {
    @Autowired
    private lateinit var workflowClient: WorkflowClient

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @GetMapping("/startOrder")
    fun startOrder(): RedirectView {
        val orderId = Random.nextLong().absoluteValue.toString()

        val workflow = workflowClient.newWorkflowStub(
            OrderWorkflowInterface::class.java,
            WorkflowOptions.newBuilder().setWorkflowId("Order:${orderId}").setTaskQueue("OrderTaskQueue").build()
        )

        WorkflowClient.start(workflow::startOrder, orderId, "mr. Payer", 100, "salami and camambert")

        return RedirectView("/")
    }

    @GetMapping("/confirmPartiallyAssembledOrder")
    fun confirmPartiallyAssembledOrder(@RequestParam("orderId") orderId: String): RedirectView {
        val order = orderRepository.findById(orderId).get()

        val workflow = workflowClient.newWorkflowStub(OrderWorkflowInterface::class.java, order.workflowId)
        workflow.confirmPartiallyAssembledOrder(order.workflowId)

        return RedirectView("/")
    }

    @GetMapping("/cancelPartiallyAssembledOrder")
    fun cancelPartiallyAssembledOrder(@RequestParam("orderId") orderId: String): RedirectView {
        val order = orderRepository.findById(orderId).get()

        val workflow = workflowClient.newWorkflowStub(OrderWorkflowInterface::class.java, order.workflowId)
        workflow.cancelPartiallyAssembledOrder(order.workflowId)

        return RedirectView("/")
    }

    @GetMapping("/")
    fun showOrders(): String {
        val manualActionButtons = fun(order: Order): String {
            if (order.status != OrderStatus.MANUAL_HANDLING) {
                return ""
            }

            return """
                    <a href="./confirmPartiallyAssembledOrder?orderId=${order.id}">confirm<a>
                    |
                    <a href="./cancelPartiallyAssembledOrder?orderId=${order.id}">cancel<a>
                """.trimIndent()
        }

        val tableRows = orderRepository
            .findAllByOrderByCreatedAtDesc()
            .joinToString("") { order ->
                """
                    <tr>
                        <td>${manualActionButtons(order)}</td>
                        <td>${order.status}</td>
                        <td>${order.id}</td>
                        <td>${order.workflowId}</td>
                        <td>${order.transactionId}</td>
                        <td>${order.customerName}</td>
                        <td>${order.amount}</td>
                        <td>${order.itemsDescription}</td>
                        <td>${order.isRisky}</td>
                        <td>${order.riskScore}</td>
                        <td>${order.cancelReason}</td>
                        <td>${order.manualHandlingReason}</td>
                        <td>${order.createdAt}</td>
                    </tr>
                """.trimIndent()
            }

        return """
            <html>
            <head>
                <style>
                    table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    th, td {
                        padding: 8px 12px;
                        border: 1px solid #ddd;
                        text-align: left;
                    }
                    th {
                        background-color: #f2f2f2;
                    }
                    tr:nth-child(even) {
                        background-color: #f9f9f9;
                    }
                    tr:hover {
                        background-color: #f1f1f1;
                    }
                </style>
            </head>
            <body>
                <p>
                    <a href="./startOrder">Start new order<a>
                </p>
                <table border="1">
                    <tr>
                        <th>actions</th>
                        <th>status</th>
                        <th>id</th>
                        <th>workflowId</th>
                        <th>transactionId</th>
                        <th>customerName</th>
                        <th>amount</th>
                        <th>itemsDescription</th>
                        <th>isRisky</th>
                        <th>riskScore</th>
                        <th>cancelReason</th>
                        <th>manualHandlingReason</th>
                        <th>createdAt</th>
                    </tr>
                    $tableRows
                </table>
            </body>
            </html>
        """.trimIndent()
    }
}
