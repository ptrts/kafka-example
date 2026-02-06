package x

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class DemoKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<Any, Any>,

    @Value($$"${app.kafka.demo-topic}")
    private val topicName: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(message: DemoMessage) {
        kafkaTemplate.send(
            topicName,
            message.id.toString(),
            message
        )

        logger.info("Demo message sent to topic '{}' : {}", topicName, message)
    }
}
