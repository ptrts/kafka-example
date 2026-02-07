package x

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class Message2Consumer {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [
            TopicNames.MESSAGE_2
        ],
        groupId = "default"
    )
    fun consume(message2: Message2) {
        logger.info("Message2 received: id={}, name={}", message2.id, message2.name)
    }
}
