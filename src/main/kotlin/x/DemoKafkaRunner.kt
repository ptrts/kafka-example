package x

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DemoKafkaRunner(
    private val kafkaTopicService: KafkaTopicService,
    private val demoKafkaProducer: DemoKafkaProducer,

    @Value($$"${app.kafka.demo-topic}")
    private val topicName: String,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun runDemo() {
        kafkaTopicService.createTopic(topicName)

        val message = DemoMessage(
            id = 1L,
            name = "demo-message",
        )
        demoKafkaProducer.send(message)
    }
}
