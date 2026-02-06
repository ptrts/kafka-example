package x

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class DemoKafkaConsumer {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [
            $$"${app.kafka.demo-topic}"
        ],
        groupId = "demo-consumer-group"
    )
    fun consume(message: DemoMessage) {
        logger.info("Demo message received: id={}, name={}", message.id, message.name)
    }
}
