package org.koin.core

import org.koin.Simple
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals

class ParametersInjectionTest {

    @Test
    @JsName("can_create_a_single_with_parameters")
fun `can create a single with parameters`() {

        val app = koinApplication {
            modules(
                module {
                    single { (i: Int) -> Simple.MySingle(i) }
                })
        }

        val koin = app.koin
        val a: Simple.MySingle = koin.get { parametersOf(42) }

        assertEquals(42, a.id)
    }

    @Test
    @JsName("can_get_a_single_created_with_parameters___no_need_of_give_it_again")
fun `can get a single created with parameters - no need of give it again`() {

        val app = koinApplication {
            modules(
                module {
                    single { (i: Int) -> Simple.MySingle(i) }
                })
        }

        val koin = app.koin
        val a: Simple.MySingle = koin.get { parametersOf(42) }

        assertEquals(42, a.id)

        val a2: Simple.MySingle = koin.get()

        assertEquals(42, a2.id)
    }

    @Test
    @JsName("can_create_factories_with_params")
fun `can create factories with params`() {

        val app = koinApplication {
            modules(
                module {
                    factory { (i: Int) -> Simple.MyIntFactory(i) }
                })
        }

        val koin = app.koin
        val a: Simple.MyIntFactory = koin.get { parametersOf(42) }
        val a2: Simple.MyIntFactory = koin.get { parametersOf(43) }

        assertEquals(42, a.id)
        assertEquals(43, a2.id)
    }

    @Test
    @JsName("chained_factory_injection")
fun `chained factory injection`() {
        val koin = koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    factory { (i: Int) -> Simple.MyIntFactory(i) }
                    factory { (s: String) -> Simple.MyStringFactory(s) }
                    factory { (i: Int, s: String) ->
                        Simple.AllFactory(
                            get { parametersOf(i) },
                            get { parametersOf(s) })
                    }
                })
        }.koin

        val f = koin.get<Simple.AllFactory> { parametersOf(42, "42") }

        assertEquals(42, f.ints.id)
        assertEquals("42", f.strings.s)
    }
}
