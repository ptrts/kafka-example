package x

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DemoKafkaRunner(
    private val demoKafkaProducer: DemoKafkaProducer,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun runDemo() {
        val message = DemoMessage(
            id = 1L,
            name = "demo-message",
        )
        demoKafkaProducer.send(message)
    }
}
