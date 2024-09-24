package demo.saga

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class HelloApiController {
    @GetMapping("/hello2")
    fun createShipment(): String = "Hello, Saga! Your number is ${UUID.randomUUID()}"
}
