package by.dismess.core.utils

import by.dismess.core.model.UserID

interface UserIDGenerator {
    fun generate(login: String): UserID
}
