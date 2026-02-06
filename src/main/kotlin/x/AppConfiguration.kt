package x

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.converter.MessageConverter
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter

@Configuration
class AppConfiguration {

    @Bean
    fun messageConverter(): MessageConverter = StringJacksonJsonMessageConverter()

    @Bean
    fun topicConfig(
        @Value($$"${app.kafka.demo-topic}")
        topicName: String,
    ) =
        NewTopic(topicName, 1, 1)
}
