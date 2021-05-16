package by.dismess.core.chating.elements.stored

import by.dismess.core.utils.UniqID

data class ChatStored(
    val id: UniqID,
    val membersID: List<UniqID>
)
