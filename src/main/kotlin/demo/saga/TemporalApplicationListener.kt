package demo.saga

import io.temporal.worker.WorkerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class TemporalApplicationListener : ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private lateinit var workerFactory: WorkerFactory

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        workerFactory.start()
    }
}
