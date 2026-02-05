package x

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import org.slf4j.LoggerFactory
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutionException

@Service
class KafkaTopicService(
    private val kafkaProperties: KafkaProperties,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun createTopic(name: String, partitions: Int = 1, replicationFactor: Short = 1) {
        val adminProperties = mapOf(
            "bootstrap.servers" to kafkaProperties.bootstrapServers.joinToString(","),
        )

        AdminClient.create(adminProperties).use { adminClient ->
            try {
                adminClient.createTopics(listOf(NewTopic(name, partitions, replicationFactor))).all().get()
                logger.info("Kafka topic '{}' has been created", name)
            } catch (ex: ExecutionException) {
                if (ex.cause is TopicExistsException) {
                    logger.info("Kafka topic '{}' already exists", name)
                    return
                }
                throw ex
            }
        }
    }
}
