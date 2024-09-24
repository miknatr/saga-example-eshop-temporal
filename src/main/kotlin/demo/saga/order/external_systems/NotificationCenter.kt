package demo.saga.order.external_systems

import org.springframework.stereotype.Component

@Component
class NotificationCenter {
    fun notifyCustomer(customerName: String, text: String) {
        // ...
        // Here should be a call to remote notifications API (email, SMS, etc.)
        // ...
    }
}
