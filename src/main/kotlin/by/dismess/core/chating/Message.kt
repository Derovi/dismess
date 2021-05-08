package by.dismess.core.chating

import by.dismess.core.model.UserID
import java.util.Date

data class Message(
        val date: Date = Date(),
        val author: UserID,
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
