package org.koin.core

import org.koin.Simple
import org.koin.core.context.*
import org.koin.core.definition.Kind
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.logger.Level
import org.koin.core.mp.KoinMultiPlatform
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.getDefinition
import kotlin.js.JsName
import kotlin.test.*

class DynamicModulesTest {

    @Test
    @JsName("should_unload_single_definition")
fun `should unload single definition`() {
        val module = module {
            single { Simple.ComponentA() }
        }
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(module)
        }

        val defA = app.getDefinition(Simple.ComponentA::class) ?: error("no definition found")
        assertEquals(Kind.Single, defA.kind)

        assertNotNull(app.koin.get<Simple.ComponentA>())

        app.unloadModules(module)

        assertNull(app.getDefinition(Simple.ComponentA::class))

        try {
            app.koin.get<Simple.ComponentA>()
            fail()
        } catch (e: NoBeanDefFoundException) {
            KoinMultiPlatform.printStackTrace(e)
        }
    }

    @Test
    @JsName("should_unload_additional_bound_definition")
fun `should unload additional bound definition`() {
        val module = module {
            single { Simple.Component1() } bind Simple.ComponentInterface1::class
        }
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(module)
        }

        val defA = app.getDefinition(Simple.Component1::class) ?: error("no definition found")
        assertEquals(Kind.Single, defA.kind)

        assertNotNull(app.koin.get<Simple.Component1>())
        assertNotNull(app.koin.get<Simple.ComponentInterface1>())

        app.unloadModules(module)

        assertNull(app.getDefinition(Simple.ComponentA::class))

        try {
            app.koin.get<Simple.Component1>()
            fail()
        } catch (e: NoBeanDefFoundException) {
            KoinMultiPlatform.printStackTrace(e)
        }

        try {
            app.koin.get<Simple.ComponentInterface1>()
            fail()
        } catch (e: NoBeanDefFoundException) {
            KoinMultiPlatform.printStackTrace(e)
        }
    }

    @Test
    @JsName("should_unload_one_module_definition")
fun `should unload one module definition`() {
        val module1 = module {
            single { Simple.ComponentA() }
        }
        val module2 = module {
            single { Simple.ComponentB(get()) }
        }
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(listOf(module1, module2))
        }

        app.getDefinition(Simple.ComponentA::class) ?: error("no definition found")
        app.getDefinition(Simple.ComponentB::class) ?: error("no definition found")

        assertNotNull(app.koin.get<Simple.ComponentA>())
        assertNotNull(app.koin.get<Simple.ComponentB>())

        app.unloadModules(module2)

        assertNull(app.getDefinition(Simple.ComponentB::class))

        try {
            app.koin.get<Simple.ComponentB>()
            fail()
        } catch (e: NoBeanDefFoundException) {
            KoinMultiPlatform.printStackTrace(e)
        }
    }

    @Test
    @JsName("should_unload_one_module_definition___factory")
fun `should unload one module definition - factory`() {
        val module1 = module {
            single { Simple.ComponentA() }
        }
        val module2 = module {
            factory { Simple.ComponentB(get()) }
        }
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(listOf(module1, module2))
        }

        app.getDefinition(Simple.ComponentA::class) ?: error("no definition found")
        app.getDefinition(Simple.ComponentB::class) ?: error("no definition found")

        assertNotNull(app.koin.get<Simple.ComponentA>())
        assertNotNull(app.koin.get<Simple.ComponentB>())

        app.unloadModules(module2)

        assertNull(app.getDefinition(Simple.ComponentB::class))

        try {
            app.koin.get<Simple.ComponentB>()
            fail()
        } catch (e: NoBeanDefFoundException) {
            KoinMultiPlatform.printStackTrace(e)
        }
    }

    @Test
    @JsName("should_unload_module_override_definition")
fun `should unload module override definition`() {
        val module1 = module {
            single { Simple.MySingle(42) }
        }
        val module2 = module(override = true) {
            single { Simple.MySingle(24) }
        }
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(listOf(module1, module2))
        }

        app.getDefinition(Simple.MySingle::class) ?: error("no definition found")
        assertEquals(24, app.koin.get<Simple.MySingle>().id)

        app.unloadModules(module2)

        assertNull(app.getDefinition(Simple.MySingle::class))

        try {
            app.koin.get<Simple.MySingle>()
            fail()
        } catch (e: NoBeanDefFoundException) {
            KoinMultiPlatform.printStackTrace(e)
        }
    }

    @Test
    @JsName("should_reload_module_definition")
fun `should reload module definition`() {
        val module = module {
            single { (id: Int) -> Simple.MySingle(id) }
        }
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(module)
        }

        app.getDefinition(Simple.MySingle::class) ?: error("no definition found")
        assertEquals(42, app.koin.get<Simple.MySingle> { parametersOf(42) }.id)

        app.unloadModules(module)
        app.modules(module)
        assertNotNull(app.getDefinition(Simple.MySingle::class))

        assertEquals(24, app.koin.get<Simple.MySingle> { parametersOf(24) }.id)
    }

    @Test
    @JsName("should_reload_module_definition___global_context")
fun `should reload module definition - global context`() {
        val module = module {
            single { (id: Int) -> Simple.MySingle(id) }
        }
        startKoin {
            printLogger(Level.DEBUG)
            modules(module)
        }

        assertEquals(42, GlobalContext.get().koin.get<Simple.MySingle> { parametersOf(42) }.id)

        unloadKoinModules(module)
        loadKoinModules(module)

        assertEquals(24, GlobalContext.get().koin.get<Simple.MySingle> { parametersOf(24) }.id)

        stopKoin()
    }

    @Test
    @JsName("should_unload_scoped_definition")
fun `should unload scoped definition`() {
        val scopeKey = named("-SCOPE-")
        val module = module {
            scope(scopeKey) {
                scoped { Simple.ComponentA() }
            }
        }
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(module)
        }

        val scope = app.koin.createScope("id", scopeKey)
        val defA = scope.beanRegistry.findDefinition(clazz = Simple.ComponentA::class)
            ?: error("no definition found")
        assertEquals(Kind.Scoped, defA.kind)
        assertEquals(scopeKey, defA.scopeName)
        assertNotNull(scope.get<Simple.ComponentA>())

        app.unloadModules(module)

        assertNull(scope.beanRegistry.findDefinition(clazz = Simple.ComponentA::class))

        try {
            scope.get<Simple.ComponentA>()
            fail()
        } catch (e: NoBeanDefFoundException) {
            KoinMultiPlatform.printStackTrace(e)
        }
    }

    @Test
    @JsName("should_reload_scoped_definition")
fun `should reload scoped definition`() {
        val scopeKey = named("-SCOPE-")
        val module = module {
            scope(scopeKey) {
                scoped { Simple.ComponentA() }
            }
        }
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(module)
        }

        var scope = app.koin.createScope("id", scopeKey)
        val defA = scope.beanRegistry.findDefinition(clazz = Simple.ComponentA::class)
            ?: error("no definition found")
        assertEquals(Kind.Scoped, defA.kind)
        assertEquals(scopeKey, defA.scopeName)
        assertNotNull(scope.get<Simple.ComponentA>())

        app.unloadModules(module)
        app.modules(module)

        scope = app.koin.createScope("id", scopeKey)
        scope.get<Simple.ComponentA>()
        assertNotNull(scope.beanRegistry.findDefinition(clazz = Simple.ComponentA::class))
    }

    @Test
    @JsName("should_reload_scoped_definition___global")
fun `should reload scoped definition - global`() {
        val scopeKey = named("-SCOPE-")
        val module = module {
            scope(scopeKey) {
                scoped { Simple.ComponentA() }
            }
        }
        startKoin {
            printLogger(Level.DEBUG)
            modules(module)
        }

        var scope = GlobalContext.get().koin.createScope("id", scopeKey)
        assertNotNull(scope.get<Simple.ComponentA>())

        unloadKoinModules(module)
        loadKoinModules(module)

        scope = GlobalContext.get().koin.createScope("id", scopeKey)
        scope.get<Simple.ComponentA>()

        stopKoin()
    }
}
