package by.dismess.core.extensions

import by.dismess.core.model.User
import by.dismess.core.utils.generateUserID

val User.id
    get() = generateUserID(login)
