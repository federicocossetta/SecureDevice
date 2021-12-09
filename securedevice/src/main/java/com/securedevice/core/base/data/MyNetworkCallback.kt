package com.securedevice.core.base.data

interface MyNetworkCallback {
    fun onCallSuccess(networkSuccess : NetworkSuccess)

    fun onCallError(networkError : NetworkError)
}