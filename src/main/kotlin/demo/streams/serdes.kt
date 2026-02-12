package demo.streams

import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonSerde

inline fun <reified T> jsonSerde(): JacksonJsonSerde<T> =
    JacksonJsonSerde(T::class.java).apply {
        // Для демо: доверяем всем пакетам (иначе могут быть проблемы при десериализации)
        configure(mapOf(JacksonJsonDeserializer.TRUSTED_PACKAGES to "*"), false)
    }
