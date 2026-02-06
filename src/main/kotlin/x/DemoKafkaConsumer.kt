package x

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class DemoKafkaConsumer(
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [
            $$"${app.kafka.demo-topic}"
        ],
        groupId = "demo-consumer-group"
    )
    fun consume(payload: String) {
        val message: DemoMessage = objectMapper.readValue(
            payload,
            DemoMessage::class.java
        )

        logger.info("Demo message received: id={}, name={}", message.id, message.name)
    }
}
