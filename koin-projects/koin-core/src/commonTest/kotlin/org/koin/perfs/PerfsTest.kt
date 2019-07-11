package org.koin.perfs

import org.koin.core.time.measureDuration
import org.koin.dsl.koinApplication
import org.koin.test.assertDefinitionsCount
import kotlin.js.JsName
import kotlin.test.Test

class PerfsTest {

    @Test
    @JsName("empty_module_perfs")
fun `empty module perfs`() {
        val (app, duration) = measureDuration {
            koinApplication {
            }
        }
        println("started in $duration ms")

        app.assertDefinitionsCount(0)
        app.close()
    }

    /*
    Perfs on MBP 2018
        started in 148.528942 ms
        measured executed in 1.03463 ms
        started in 0.966499 ms
        measured executed in 0.036298 ms
     */
    @Test
    @JsName("perfModule400_module_perfs")
fun `perfModule400 module perfs`() {
        runPerfs()
        runPerfs()
    }

    private fun runPerfs() {
        val (app, duration) = measureDuration {
            koinApplication {
                modules(perfModule400)
            }
        }
        println("started in $duration ms")

        app.assertDefinitionsCount(400)

        val koin = app.koin

        val (_, executionDuration) = measureDuration {
            koin.get<Perfs.A27>()
            koin.get<Perfs.B31>()
            koin.get<Perfs.C12>()
            koin.get<Perfs.D42>()
        }
        println("measured executed in $executionDuration ms")

        app.close()
    }
}
