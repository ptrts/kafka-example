package x

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class DemoKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${app.kafka.demo-topic}") private val topicName: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(message: DemoMessage) {
        val payload = objectMapper.writeValueAsString(message)
        kafkaTemplate.send(topicName, message.id.toString(), payload)
        logger.info("Demo message sent to topic '{}' : {}", topicName, payload)
    }
}
