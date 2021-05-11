package by.dismess.core.chating.elements

import by.dismess.core.utils.UniqID

data class KeyMessage(
    val key: ByteArray,
    val chatID: UniqID,
    val senderID: UniqID,
    val sendBack: Boolean
)
