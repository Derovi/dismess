package by.dismess.core.chating.elements.stored

import by.dismess.core.chating.elements.Message
import by.dismess.core.chating.elements.id.ChunkID

class ChunkStored(
    val id: ChunkID,
    val complete: Boolean,
    val messages: List<Message>
)
