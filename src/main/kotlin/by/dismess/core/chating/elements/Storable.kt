package by.dismess.core.chating.elements

interface Storable {
    /**
     * Stores to local storage
     * Throws exception
     * Use when objects changed remotely
     * and changed should be remembered
     */
    suspend fun store()

    /**
     * Stores to DHT
     * False if error
     * Must call store() inside
     * Use when objects changed locally
     */
    suspend fun publish(): Boolean
}