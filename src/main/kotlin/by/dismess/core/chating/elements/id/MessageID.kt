package by.dismess.core.chating.elements.id

import by.dismess.core.utils.UniqID
import by.dismess.core.utils.groupID
import by.dismess.core.utils.uniqID

data class MessageID(val chunkID: UniqID, val index: Int) {
    val uniqID = groupID(chunkID, index.uniqID)
}