package org.koin.dsl

import org.koin.Simple
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.logger.Level
import org.koin.core.mp.KoinMultiPlatform
import org.koin.core.qualifier.named
import org.koin.multiplatform.doInOtherThread
import org.koin.test.assertDefinitionsCount
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class AdditionalTypeBindingTest {

    @Test
    fun `can resolve an additional type - bind`() {
        val app = doInOtherThread {
            koinApplication {
                printLogger()
                modules(
                    module {
                        single { Simple.Component1() } bind Simple.ComponentInterface1::class
                    })
            }
        }

        doInOtherThread {
            app.assertDefinitionsCount(1)

            val koin = app.koin
            val c1 = koin.get<Simple.Component1>()
            val c = koin.bind<Simple.ComponentInterface1, Simple.Component1>()

            assertEquals(c1, c)
        }

    }

    @Test
    fun `can resolve an additional type`() {
        val app = doInOtherThread {
            koinApplication {
                printLogger()
                modules(
                    module {
                        single { Simple.Component1() } bind Simple.ComponentInterface1::class
                    })
            }
        }

        doInOtherThread {
            app.assertDefinitionsCount(1)

            val koin = app.koin
            val c1 = koin.get<Simple.Component1>()
            val c = koin.get<Simple.ComponentInterface1>()

            assertEquals(c1, c)
        }

    }

    @Test
    fun `can't resolve an additional type`() {
        val app = doInOtherThread{
            koinApplication {
                printLogger(Level.DEBUG)
                modules(
                    module {
                        single { Simple.Component1() } bind Simple.ComponentInterface1::class
                        single { Simple.Component2() } bind Simple.ComponentInterface1::class
                    })
            }
        }

        doInOtherThread{
            app.assertDefinitionsCount(2)

            val koin = app.koin
            try {
                koin.get<Simple.ComponentInterface1>()
                fail()
            } catch (e: NoBeanDefFoundException) {
                KoinMultiPlatform.printStackTrace(e)
            }

            assertNotEquals(
                koin.bind<Simple.ComponentInterface1, Simple.Component1>(),
                koin.bind<Simple.ComponentInterface1, Simple.Component2>()
            )
        }
    }

    @Test
    fun `can resolve an additional type in DSL`() {
        val app = doInOtherThread{
            koinApplication {
                printLogger(Level.DEBUG)
                modules(
                    module {
                        single { Simple.Component1() } bind Simple.ComponentInterface1::class
                        single { Simple.Component2() } bind Simple.ComponentInterface1::class
                        single { Simple.UserComponent(bind<Simple.ComponentInterface1, Simple.Component1>()) }
                    })
            }
        }

        doInOtherThread{
            app.assertDefinitionsCount(3)

            val koin = app.koin
            assertEquals(koin.get<Simple.UserComponent>().c1, koin.get<Simple.Component1>())
        }
    }

    @Test
    fun `additional type conflict`() {
        val koin = doInOtherThread{
            koinApplication {
                printLogger()
                modules(
                    module {
                        single { Simple.Component2() } bind Simple.ComponentInterface1::class
                        single<Simple.ComponentInterface1> { Simple.Component1() }
                    })
            }.koin
        }

        doInOtherThread{
            assert(koin.getAll<Simple.ComponentInterface1>().size == 2)

            assertTrue(koin.get<Simple.ComponentInterface1>() is Simple.Component1)
        }
    }

    @Test
    fun `should not conflict name & default type`() {
        val app = doInOtherThread{
            koinApplication {
                printLogger()
                modules(
                    module {
                        single<Simple.ComponentInterface1>(named("default")) { Simple.Component2() }
                        single<Simple.ComponentInterface1> { Simple.Component1() }
                    })
            }
        }
        doInOtherThread{
            val koin = app.koin
            koin.get<Simple.ComponentInterface1>(named("default"))
        }
    }

    @Test
    fun `can resolve an additional types`() {
        val app = doInOtherThread{
            koinApplication {
                modules(
                    module {
                        single { Simple.Component1() } binds arrayOf(
                            Simple.ComponentInterface1::class,
                            Simple.ComponentInterface2::class
                        )
                    })
            }
        }

        doInOtherThread{
            app.assertDefinitionsCount(1)

            val koin = app.koin
            val c1 = koin.get<Simple.Component1>()
            val ci1 = koin.bind<Simple.ComponentInterface1, Simple.Component1>()
            val ci2 = koin.bind<Simple.ComponentInterface2, Simple.Component1>()

            assertEquals(c1, ci1)
            assertEquals(c1, ci2)
        }
    }

    @Test
    fun `conflicting with additional types`() {
        val koin = doInOtherThread{
            koinApplication {
                modules(
                    module {
                        single<Simple.ComponentInterface1> { Simple.Component2() }
                        single { Simple.Component1() } binds arrayOf(
                            Simple.ComponentInterface1::class,
                            Simple.ComponentInterface2::class
                        )
                    })
            }.koin
        }

        doInOtherThread{ assert(koin.getAll<Simple.ComponentInterface1>().size == 2) }
    }
}
