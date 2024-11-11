package demo.saga.order.domain

import demo.saga.order.domain.OrderStatus.*
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "orders")
class Order private constructor(
    @Id
    @Column(nullable = false)
    val id: String,

    @Column(nullable = false)
    val workflowId: String,

    @Column(nullable = false)
    val customerName: String,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    val itemsDescription: String,

    @Column
    var isRisky: Boolean? = null,

    @Column
    var riskScore: Int? = null,

    @Column
    var transactionId: String? = null,

    @Column
    var cancelReason: String? = null,

    @Column
    var manualHandlingReason: String? = null,

    @Column
    var isHandledManually: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,

    @Column(nullable = false)
    val createdAt: OffsetDateTime,

    @Column
    var updatedAt: OffsetDateTime? = null,

    @Column
    var completedAt: OffsetDateTime? = null
) {
    companion object {
        fun constructOrder(
            orderId: String,
            workflowId: String,
            customerName: String,
            amount: Long,
            itemsDescription: String
        ) =
            Order(
                id = orderId,
                workflowId = workflowId,
                customerName = customerName,
                amount = amount,
                itemsDescription = itemsDescription,
                status = CREATED,
                createdAt = OffsetDateTime.now()
            )
    }

    fun startHolding() {
        status = HOLDING
        updatedAt = OffsetDateTime.now()
    }

    fun markAsHeld(transactionIdFromProvider: String) {
        status = HELD
        transactionId = transactionIdFromProvider
        updatedAt = OffsetDateTime.now()
    }

    fun startUnholding() {
        status = UNHOLDING
        updatedAt = OffsetDateTime.now()
    }

    fun markAsUnheld() {
        status = UNHELD
        updatedAt = OffsetDateTime.now()
    }

    fun startChecking() {
        status = CHECKING
        updatedAt = OffsetDateTime.now()
    }

    fun markAsChecked(calculatedRiskScore: Int, isCustomerRisky: Boolean) {
        status = CHECKED
        isRisky = isCustomerRisky
        riskScore = calculatedRiskScore
        updatedAt = OffsetDateTime.now()
    }

    fun startOrdering() {
        status = ORDERING
        updatedAt = OffsetDateTime.now()
    }

    fun markAsOrdered() {
        status = ORDERED
        updatedAt = OffsetDateTime.now()
    }

    fun startCancelingAtWarehouse() {
        status = CANCELING_WAREHOUSE
        updatedAt = OffsetDateTime.now()
    }

    fun markAsCanceledAtWarehouse() {
        status = CANCELED_WAREHOUSE
        updatedAt = OffsetDateTime.now()
    }

    fun startCapturing() {
        status = CAPTURING
        updatedAt = OffsetDateTime.now()
    }

    fun markAsCaptured() {
        status = CAPTURED
        updatedAt = OffsetDateTime.now()
    }

    fun startNotifyAboutSuccess() {
        status = SUCCESS_NOTIFYING
        updatedAt = OffsetDateTime.now()
    }

    fun markAsNotifiedAboutSuccess() {
        status = SUCCESS_NOTIFIED
        updatedAt = OffsetDateTime.now()
    }

    fun startNotifyAboutCancel() {
        status = CANCEL_NOTIFYING
        updatedAt = OffsetDateTime.now()
    }

    fun markAsNotifiedAboutCancel() {
        status = CANCEL_NOTIFIED
        updatedAt = OffsetDateTime.now()
    }

    fun markAsPaidAndDispatched() {
        status = PAID_AND_DISPATCHED
        updatedAt = OffsetDateTime.now()
        completedAt = OffsetDateTime.now()
    }

    fun markAsHandledManually() {
        isHandledManually = true
        updatedAt = OffsetDateTime.now()
    }

    fun cancel(reason: String) {
        status = CANCELLED
        cancelReason = reason
        updatedAt = OffsetDateTime.now()
        completedAt = OffsetDateTime.now()
    }

    fun moveToManualHandling(reason: String) {
        status = MANUAL_HANDLING
        manualHandlingReason = reason
        updatedAt = OffsetDateTime.now()
    }
}
