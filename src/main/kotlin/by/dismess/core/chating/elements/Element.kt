package by.dismess.core.chating.elements

/**
 * Elements are objects in distributed system
 * Elements belong to us called OWN
 * Elements belong to somebody else called OTHER
 */
interface Element {
    suspend fun persist(): Boolean
}
