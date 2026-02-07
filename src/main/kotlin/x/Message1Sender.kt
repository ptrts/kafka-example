package x

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class Message1Sender(
    private val kafkaTemplate: KafkaTemplate<Any, Any>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(message: Message1) {
        kafkaTemplate.send(
            TopicNames.MESSAGE_1,
            message.id.toString(),
            message
        )

        logger.info("Demo message sent to topic '{}' : {}", TopicNames.MESSAGE_1, message)
    }
}
