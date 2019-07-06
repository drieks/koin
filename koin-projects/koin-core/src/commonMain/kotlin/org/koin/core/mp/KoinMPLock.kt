package org.koin.core.mp

abstract class AbstractKoinMPLock<T> {
    abstract val subject: T

    open operator fun <R> invoke(handler: T.() -> R): R {
        return subject.handler()
    }

    open fun close(){}
}

expect class KoinMPLock<T>(subject: T) : AbstractKoinMPLock<T>
