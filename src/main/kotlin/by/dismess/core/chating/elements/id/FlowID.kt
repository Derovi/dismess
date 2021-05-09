package by.dismess.core.chating.elements.id

import by.dismess.core.utils.UniqID
import by.dismess.core.utils.groupID

data class FlowID(
    val chatID: UniqID,
    val authorID: UniqID
) {
    val uniqID: UniqID = groupID(chatID, authorID)
}
