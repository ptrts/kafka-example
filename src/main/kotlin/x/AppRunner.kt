package x

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class AppRunner(
    private val kafkaTemplate: KafkaTemplate<Any, Any>
) {

    @EventListener(ApplicationReadyEvent::class)
    fun runDemo() {
        val message = Message1(
            id = 1L,
            name = "demo-message",
        )
        kafkaTemplate.executeInTransaction {
            it.send(TopicNames.MESSAGE_1, message.id.toString(), message)
        }
    }
}
