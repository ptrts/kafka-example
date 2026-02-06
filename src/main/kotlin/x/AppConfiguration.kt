package x

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfiguration {

    @Bean
    fun topicConfig(
        @Value($$"${app.kafka.demo-topic}")
        topicName: String,
    ) =
        NewTopic(topicName, 1, 1)
}
