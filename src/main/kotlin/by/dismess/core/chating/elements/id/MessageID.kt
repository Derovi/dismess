package by.dismess.core.chating.elements.id

import by.dismess.core.utils.groupID
import by.dismess.core.utils.uniqID

data class MessageID(val chunkID: ChunkID, val index: Int) {
    val uniqID = groupID(chunkID.uniqID, index.uniqID)
}