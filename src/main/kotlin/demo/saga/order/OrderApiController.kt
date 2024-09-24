package demo.saga.order

import demo.saga.order.domain.OrderRepository
import demo.saga.order.saga.OrderWorkflowFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderApiController {
    @Autowired
    private lateinit var workflowFactory: OrderWorkflowFactory

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @GetMapping("/processOrder")
    fun processOrder(): String {
        val workflow = workflowFactory.createWorkflow()

        val orderFinalState = workflow
            .processOrder("mr. Payer", 100, "salami and camambert")

        val workflowId = workflowFactory.toStub(workflow).execution.workflowId

        return "workflowId: $workflowId, $orderFinalState"
    }

    @GetMapping("/confirmOrder")
    fun confirmOrder(@RequestParam("orderId") orderId: String): String {
        // TODO example with signals
        val order = orderRepository.findById(orderId).get()

        val workflow = workflowFactory.getWorkflow(order.workflowId)
        workflow.signal("confirmOrderManually")

        return "workflowId: ${order.workflowId}"
    }

    @GetMapping("/showOrders")
    fun showOrder(): String {
        val tableRows = orderRepository
            .findAll()
            .joinToString("") { order ->
                """
                    <tr>
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
                        <td>${order.updatedAt}</td>
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
                <table border="1">
                    <tr>
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
                        <th>updatedAt</th>
                    </tr>
                    $tableRows
                </table>
            </body>
            </html>
        """.trimIndent()
    }
}
