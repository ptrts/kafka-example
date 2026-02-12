package demo.streams

data class ClickEvent(
    val clickId: String,
    val userId: String,
    val sessionId: String,
    val productId: String,
    val ts: Long,
)

data class OrderItem(
    val productId: String,
    val qty: Int,
)

data class OrderEvent(
    val orderId: String,
    val userId: String,
    val sessionId: String,
    val items: List<OrderItem>,
    val total: Double,
    val ts: Long,
)

enum class PaymentStatus { APPROVED, DECLINED }

data class PaymentEvent(
    val paymentId: String,
    val orderId: String,
    val status: PaymentStatus,
    val amount: Double,
    val ts: Long,
)

enum class ShipmentStatus { CREATED, SHIPPED, DELIVERED }

data class ShipmentEvent(
    val shipmentId: String,
    val orderId: String,
    val status: ShipmentStatus,
    val ts: Long,
)

data class ProductRef(
    val productId: String,
    val category: String,
    val price: Double,
    val updatedTs: Long,
)

data class EnrichedClick(
    val clickId: String,
    val userId: String,
    val sessionId: String,
    val productId: String,
    val category: String?,
    val price: Double?,
    val ts: Long,
)

data class ClickToOrder(
    val userId: String,
    val sessionId: String,
    val clickId: String,
    val orderId: String,
    val productId: String,
    val category: String?,
    val clickTs: Long,
    val orderTs: Long,
)

enum class OrderStage { CREATED, PAYMENT_APPROVED, PAYMENT_DECLINED, SHIPPED, DELIVERED }

data class OrderState(
    val orderId: String,
    val userId: String,
    val sessionId: String,
    val total: Double,
    val stage: OrderStage,
    val createdTs: Long,
    val lastUpdateTs: Long,
)

data class PaidOrderLine(
    val orderId: String,
    val category: String?,
    val amount: Double,
    val ts: Long,
)

data class Alert(
    val type: String,
    val orderId: String?,
    val userId: String?,
    val details: String,
    val ts: Long,
)
