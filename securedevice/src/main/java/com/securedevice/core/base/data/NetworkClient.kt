package com.securedevice.core.base.data

abstract class NetworkClient {

    /**
     * Use this method
     */
    abstract fun makeNetworkCall(url:String,networkCallback: MyNetworkCallback)

}
