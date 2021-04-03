package by.dismess.core.model

import java.util.Date

data class Message(
    val date: Date = Date(),
    val author: UserID,
    val text: String,
    val attachments: List<Attachment> = listOf()
)
