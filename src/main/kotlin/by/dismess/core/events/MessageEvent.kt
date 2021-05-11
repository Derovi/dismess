package by.dismess.core.events

import by.dismess.core.chating.elements.Message

class MessageEvent(
    val message: Message
) : Event
