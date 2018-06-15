package org.koin.dsl.module

import org.koin.core.KoinContext
import org.koin.dsl.context.ModuleDefinition
import org.koin.dsl.path.Path
import org.koin.standalone.StandAloneContext


/**
 * Create a Module
 * @Deprecated @see module
 *
 * @author - Arnaud GIULIANI
 */
@Deprecated(
    "use module function instead", ReplaceWith(
        "module { ModuleDefinition(Path.ROOT, StandAloneContext.koinContext as KoinContext).apply(init) }",
        "org.koin.dsl.context.ModuleDefinition",
        "org.koin.core.path.Path",
        "org.koin.standalone.StandAloneContext",
        "org.koin.core.KoinContext"
    )
)
fun applicationContext(init: ModuleDefinition.() -> Unit): Module = module(Path.ROOT, false, false, init)

/**
 * Create a Module
 * Gather definitions
 * @param path : Path of the module
 * @param eager : module definitions will be tagged as `eager`
 * @param override : allow all definitions from module to override definitions
 */
fun module(
    path: String = Path.ROOT,
    eager: Boolean = false,
    override: Boolean = false,
    init: ModuleDefinition.() -> Unit
): Module =
    { ModuleDefinition(path, eager, override, StandAloneContext.koinContext as KoinContext).apply(init) }

/**
 * Module - function that gives a module
 */
typealias Module = () -> ModuleDefinition