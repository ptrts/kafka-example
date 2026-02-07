package x

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class AppRunner(
    private val message1Sender: Message1Sender,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun runDemo() {
        val message = Message1(
            id = 1L,
            name = "demo-message",
        )
        message1Sender.send(message)
    }
}
