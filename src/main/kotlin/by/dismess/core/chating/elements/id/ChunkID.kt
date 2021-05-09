package by.dismess.core.chating.elements.id

import by.dismess.core.utils.UniqID
import by.dismess.core.utils.groupID
import by.dismess.core.utils.uniqID

data class ChunkID(val chatID: UniqID,
                   val authorID: UniqID,
                   val index: Int) {
    val uniqID: UniqID = groupID(chatID, authorID, index.uniqID)
}
