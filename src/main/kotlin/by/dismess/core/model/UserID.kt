package by.dismess.core.model

import java.math.BigInteger

data class UserID(var rawID: BigInteger) {
    override fun toString() = rawID.toString()
    constructor(raw: String) : this(BigInteger(raw))
}
