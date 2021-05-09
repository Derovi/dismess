package by.dismess.core.chating.elements

import by.dismess.core.chating.Attachment
import by.dismess.core.utils.UniqID
import java.util.Date

data class Message(
    val date: Date = Date(),
    val chatID: UniqID,
    val senderID: UniqID,
    val text: String,
    val attachments: MutableList<Attachment> = mutableListOf()
) {
    val byteSize: Int
        get() {
            var result = text.length
            for (attachment in attachments) {
                result += attachment.data.size
            }
            return result
        }
}
