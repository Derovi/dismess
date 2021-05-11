package by.dismess.core.services

import by.dismess.core.outer.StorageInterface
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject

class StorageServiceTest : KoinTest {

    private val storageService by inject<StorageService>()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(
            module {
                single { StorageService(get()) }
                single<StorageInterface> {
                    object : StorageInterface {
                        private val map = hashMapOf<String, ByteArray>()

                        override suspend fun exists(key: String): Boolean = map.containsKey(key)

                        override suspend fun saveRawData(key: String, data: ByteArray) {
                            map[key] = data
                        }

                        override suspend fun loadRawData(key: String): ByteArray? = map[key]
                        override suspend fun forget(key: String) {
                            map.remove(key)
                        }
                    }
                }
            }
        )
    }

    data class DClass(var a: String, var b: Int)

    @Test
    fun test() {
        runBlocking {
            Assert.assertFalse(storageService.exists("erg"))
            Assert.assertNull(storageService.loadRaw(""))
            storageService.save("key", DClass("abacaba", 75656346))
            Assert.assertEquals(storageService.load<DClass>("key"), DClass("abacaba", 75656346))
            storageService.save("int", 4254545)
            Assert.assertEquals(storageService.load<Int>("int"), 4254545)
            storageService.save("str", "45436666644")
            Assert.assertEquals(storageService.load<String>("str"), "45436666644")
            storageService.save("str", "rgeeheh")
            Assert.assertEquals(storageService.load<String>("str"), "rgeeheh")
            storageService.forget("str")
            Assert.assertFalse(storageService.exists("str"))
            storageService.saveRaw("raw", """{ "a": "str", "b": -5 }""")
            Assert.assertEquals(storageService.loadRaw("raw"), """{ "a": "str", "b": -5 }""")
            Assert.assertEquals(storageService.load<DClass>("raw"), DClass("str", -5))
        }
    }
}
