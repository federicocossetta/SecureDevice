package com.securedevice.core.base.data


data class NetworkError(var exception: Throwable) {
    constructor(error: String) : this(Exception(error))
}
