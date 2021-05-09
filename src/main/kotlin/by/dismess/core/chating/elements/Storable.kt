package by.dismess.core.chating.elements

/**
 * Elements are objects in distributed system
 * Elements belong to us called OWN
 * Elements belong to somebody else called OTHER
 * If OWN object changed locally, it can be stored to DHT (PERSIST)
 * If OTHER object changed locally, it can be stored to local storage (ACCEPT)
 */
interface Element {
    suspend fun accept()
    suspend fun persist(): Boolean
}
