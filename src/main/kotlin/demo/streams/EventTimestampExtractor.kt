package demo.streams

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.processor.TimestampExtractor

class EventTimestampExtractor : TimestampExtractor {
    override fun extract(record: ConsumerRecord<Any, Any>, partitionTime: Long): Long {
        val v = record.value()
        val ts = when (v) {
            is ClickEvent -> v.ts
            is OrderEvent -> v.ts
            is PaymentEvent -> v.ts
            is ShipmentEvent -> v.ts
            is ProductRef -> v.updatedTs
            is EnrichedClick -> v.ts
            is ClickToOrder -> v.orderTs
            is PaidOrderLine -> v.ts
            is Alert -> v.ts
            else -> null
        }
        return ts ?: record.timestamp()
    }
}
