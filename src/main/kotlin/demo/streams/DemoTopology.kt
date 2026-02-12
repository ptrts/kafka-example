package demo.streams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import java.time.Duration

@Configuration
@EnableKafkaStreams // можно убрать, если у тебя Streams и так автоконфигурирован
class DemoTopology {

    object Topics {
        const val CLICKS = "events.clicks"
        const val ORDERS = "events.orders"
        const val PAYMENTS = "events.payments"
        const val SHIPMENTS = "events.shipments"
        const val PRODUCTS = "ref.products"

        const val OUT_CLICK_TO_ORDER = "out.click-to-order"
        const val OUT_ORDER_STATE = "out.order-state"
        const val OUT_REVENUE_BY_CATEGORY = "out.revenue-by-category"
        const val OUT_ALERTS = "out.alerts"
    }

    private fun sessionKey(userId: String, sessionId: String) = "$userId|$sessionId"

    @Bean
    fun buildTopology(builder: StreamsBuilder) {
        val stringSerde = Serdes.String()

        // 1) Справочник: продукты как GlobalKTable (наглядно показывает reference-data)
        val products: GlobalKTable<String, ProductRef> =
            builder.globalTable(
                Topics.PRODUCTS,
                Consumed.with(stringSerde, jsonSerde<ProductRef>()),
                Materialized.`as`<String, ProductRef, KeyValueStore<org.apache.kafka.common.utils.Bytes, ByteArray>>("products-store")
            )

        // 2) Clicks: enrichment через join с GlobalKTable по productId
        val clicks: KStream<String, ClickEvent> =
            builder.stream(Topics.CLICKS, Consumed.with(stringSerde, jsonSerde<ClickEvent>()))

        val enrichedClicks: KStream<String, EnrichedClick> =
            clicks.join(
                products,
                { _, v -> v.productId }, // foreign key
                { click, prod ->
                    EnrichedClick(
                        clickId = click.clickId,
                        userId = click.userId,
                        sessionId = click.sessionId,
                        productId = click.productId,
                        category = prod?.category,
                        price = prod?.price,
                        ts = click.ts,
                    )
                }
            )
                // дальше ключуем по sessionKey
                .selectKey { _, v -> sessionKey(v.userId, v.sessionId) }

        // 3) Orders: читаем как stream, делаем два представления: по orderId и по sessionKey
        val ordersRaw: KStream<String, OrderEvent> =
            builder.stream(Topics.ORDERS, Consumed.with(stringSerde, jsonSerde<OrderEvent>()))

        val ordersById: KStream<String, OrderEvent> =
            ordersRaw.selectKey { _, v -> v.orderId }

        val ordersBySession: KStream<String, OrderEvent> =
            ordersRaw.selectKey { _, v -> sessionKey(v.userId, v.sessionId) }

        // 4) Join clicks -> orders в окне (конверсия/атрибуция)
        val clickToOrder: KStream<String, ClickToOrder> =
            enrichedClicks.join(
                ordersBySession,
                { click, order ->
                    ClickToOrder(
                        userId = click.userId,
                        sessionId = click.sessionId,
                        clickId = click.clickId,
                        orderId = order.orderId,
                        productId = click.productId,
                        category = click.category,
                        clickTs = click.ts,
                        orderTs = order.ts,
                    )
                },
                JoinWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(30), Duration.ofMinutes(5)),
                StreamJoined.with(stringSerde, jsonSerde<EnrichedClick>(), jsonSerde<OrderEvent>())
            )

        clickToOrder.to(
            Topics.OUT_CLICK_TO_ORDER,
            Produced.with(stringSerde, jsonSerde<ClickToOrder>())
        )

        // 5) OrderState: строим KTable состояния заказа
        val initialStateTable: KTable<String, OrderState> =
            ordersById
                .mapValues { o ->
                    OrderState(
                        orderId = o.orderId,
                        userId = o.userId,
                        sessionId = o.sessionId,
                        total = o.total,
                        stage = OrderStage.CREATED,
                        createdTs = o.ts,
                        lastUpdateTs = o.ts,
                    )
                }
                .groupByKey(Grouped.with(stringSerde, jsonSerde<OrderState>()))
                .reduce({ _, new -> new }, Materialized.`as`("order-state-created-store"))

        val payments: KStream<String, PaymentEvent> =
            builder.stream(Topics.PAYMENTS, Consumed.with(stringSerde, jsonSerde<PaymentEvent>()))
                .selectKey { _, p -> p.orderId }

        val paymentsLatest: KTable<String, PaymentEvent> =
            payments
                .groupByKey(Grouped.with(stringSerde, jsonSerde<PaymentEvent>()))
                .reduce({ _, new -> new }, Materialized.`as`("payments-latest-store"))

        val withPayment: KTable<String, OrderState> =
            initialStateTable.leftJoin(
                paymentsLatest,
                { st, pay ->
                    if (pay == null) st
                    else {
                        val stage = when (pay.status) {
                            PaymentStatus.APPROVED -> OrderStage.PAYMENT_APPROVED
                            PaymentStatus.DECLINED -> OrderStage.PAYMENT_DECLINED
                        }
                        st.copy(stage = stage, lastUpdateTs = maxOf(st.lastUpdateTs, pay.ts))
                    }
                },
                Materialized.`as`("order-state-with-payment-store")
            )

        val shipments: KStream<String, ShipmentEvent> =
            builder.stream(Topics.SHIPMENTS, Consumed.with(stringSerde, jsonSerde<ShipmentEvent>()))
                .selectKey { _, s -> s.orderId }

        val shipmentsLatest: KTable<String, ShipmentEvent> =
            shipments
                .groupByKey(Grouped.with(stringSerde, jsonSerde<ShipmentEvent>()))
                .reduce({ _, new -> new }, Materialized.`as`("shipments-latest-store"))

        val orderState: KTable<String, OrderState> =
            withPayment.leftJoin(
                shipmentsLatest,
                { st, sh ->
                    if (sh == null) st
                    else {
                        val stage = when (sh.status) {
                            ShipmentStatus.CREATED -> st.stage
                            ShipmentStatus.SHIPPED -> OrderStage.SHIPPED
                            ShipmentStatus.DELIVERED -> OrderStage.DELIVERED
                        }
                        st.copy(stage = stage, lastUpdateTs = maxOf(st.lastUpdateTs, sh.ts))
                    }
                },
                Materialized.`as`("order-state-final-store")
            )

        orderState
            .toStream()
            .to(Topics.OUT_ORDER_STATE, Produced.with(stringSerde, jsonSerde<OrderState>()))

        // 6) Revenue by category (по APPROVED), окно + suppression
        // Берем ordersById join payments (stream-stream) -> paid orders, дальше размножаем по items и обогащаем категорией.
        val paidOrders: KStream<String, Pair<OrderEvent, PaymentEvent>> =
            ordersById.join(
                payments,
                { o, p -> o to p },
                JoinWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(30), Duration.ofMinutes(10)),
                StreamJoined.with(stringSerde, jsonSerde<OrderEvent>(), jsonSerde<PaymentEvent>())
            )
                .filter { _, (o, p) -> p.status == PaymentStatus.APPROVED && p.amount > 0.0 && o.total > 0.0 }

        val paidLines: KStream<String, PaidOrderLine> =
            paidOrders
                .flatMap { _, (o, p) ->
                    o.items.map { item ->
                        KeyValue(
                            item.productId,
                            PaidOrderLine(
                                orderId = o.orderId,
                                category = null,
                                amount = (p.amount / o.items.size.coerceAtLeast(1)), // демо-распределение суммы
                                ts = maxOf(o.ts, p.ts),
                            )
                        )
                    }
                }
                // Join с products по productId (ключ уже productId)
                .join(
                    products,
                    { k, _ -> k },
                    { line, prod -> line.copy(category = prod?.category) }
                )
                // ключуем по category для агрегации
                .selectKey { _, v -> v.category ?: "UNKNOWN" }

        val revenueByCategory: KTable<Windowed<String>, Double> =
            paidLines
                .groupByKey(Grouped.with(stringSerde, jsonSerde<PaidOrderLine>()))
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(30)))
                .aggregate(
                    { 0.0 },
                    { _, v, agg -> agg + v.amount },
                    Materialized.`as`("revenue-1m-store")
                )
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))

        // В выходной топик положим ключ "category|windowStart" и сумму
        revenueByCategory
            .toStream()
            .map { wk, sum ->
                val key = wk.key() + "|" + wk.window().startTime().toString()
                KeyValue(key, sum)
            }
            .to(Topics.OUT_REVENUE_BY_CATEGORY, Produced.with(stringSerde, Serdes.Double()))

        // 7) Alerts (DSL): declined payments + крупные approved
        val declinedAlerts: KStream<String, Alert> =
            payments
                .filter { _, p -> p.status == PaymentStatus.DECLINED }
                .mapValues { p ->
                    Alert(
                        type = "PAYMENT_DECLINED",
                        orderId = p.orderId,
                        userId = null,
                        details = "paymentId=${p.paymentId}, amount=${p.amount}",
                        ts = p.ts
                    )
                }

        val highValueAlerts: KStream<String, Alert> =
            paidOrders
                .filter { _, (o, p) -> p.amount >= 500.0 }
                .mapValues { (o, p) ->
                    Alert(
                        type = "HIGH_VALUE_ORDER",
                        orderId = o.orderId,
                        userId = o.userId,
                        details = "amount=${p.amount}",
                        ts = maxOf(o.ts, p.ts)
                    )
                }

        declinedAlerts
            .merge(highValueAlerts)
            .to(Topics.OUT_ALERTS, Produced.with(stringSerde, jsonSerde<Alert>()))
    }
}
