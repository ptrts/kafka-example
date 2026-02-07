package x

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class Message2Sender(
    private val kafkaTemplate: KafkaTemplate<Any, Any>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(message: Message2) {
        kafkaTemplate.send(
            TopicNames.MESSAGE_2,
            message.id.toString(),
            message
        )

        logger.info("Demo message sent to topic '{}' : {}", TopicNames.MESSAGE_2, message)
    }
}
