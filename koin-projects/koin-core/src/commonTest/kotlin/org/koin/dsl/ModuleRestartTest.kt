package org.koin.dsl

import org.koin.Simple
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import kotlin.js.JsName
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ModuleRestartTest : KoinComponent {

    @BeforeTest
    fun before() {
        startKoin {
            modules(module {
                single { Simple.ComponentA() }
            })
        }
    }

    @AfterTest
    fun after() {
        stopKoin()
    }

    @Test
    @JsName("first_test")
fun `1st test`() {
        get<Simple.ComponentA>()
    }

    @Test
    @JsName("second_test")
fun `2nd test`() {
        get<Simple.ComponentA>()
    }
}
