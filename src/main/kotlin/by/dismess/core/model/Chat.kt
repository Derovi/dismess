package by.dismess.core.model

import by.dismess.core.utils.UniqID

data class Chat(val id: UniqID, val users: MutableList<UserID>)
