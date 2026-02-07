package x

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class Message1Consumer(
    private val message2Sender: Message2Sender,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [
            TopicNames.MESSAGE_1
        ],
        groupId = "default"
    )
    fun consume(message1: Message1) {

        logger.info("Message1 received: id={}, name={}", message1.id, message1.name)

        val message2 = Message2(
            message1.id,
            message1.name
        )

        message2Sender.send(
            message2
        )
    }
}
