package x

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class Message1Consumer(
    private val kafkaTemplate: KafkaTemplate<Any, Any>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [
            TopicNames.MESSAGE_1
        ],
        groupId = "Message1Consumer"
    )
    fun consume(message1: Message1, @Header(KafkaHeaders .RECEIVED_TOPIC) topicName: String) {

        logger.info("Message1 received: topic={} id={}, name={}", topicName, message1.id, message1.name)

        throw RuntimeException()

        /*val message2 = Message2(
            message1.id,
            message1.name
        )

        kafkaTemplate.send(
            TopicNames.MESSAGE_2,
            message2.id.toString(),
            message2
        )*/
    }
}
