package com.securedevice.core.interfaces

interface ContentProvider {

    fun notifyContentReady(path: String)

    fun notifyError(exc: Throwable?)

}