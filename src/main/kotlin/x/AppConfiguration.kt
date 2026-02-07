package x

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.support.converter.MessageConverter
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter

@Configuration
class AppConfiguration {

    @Bean
    fun messageConverter(): MessageConverter = StringJacksonJsonMessageConverter()

    @Bean
    fun topics() = KafkaAdmin.NewTopics(
        TopicBuilder.name(TopicNames.MESSAGE_1)
            .partitions(1)
            .replicas(1)
            .build(),
        TopicBuilder.name(TopicNames.MESSAGE_2)
            .partitions(1)
            .replicas(1)
            .build(),
    )
}
