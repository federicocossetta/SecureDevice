package com.securedevice.core.base

import android.content.Context
import android.util.Log
import com.securedevice.core.base.data.NetworkClient
import com.securedevice.core.base.MissingParameterException
import java.io.File
import java.lang.ref.WeakReference

class SecurityProviderOption(internal var networkClient: NetworkClient, internal var context:
WeakReference<Context>, private var path: String?, internal var createDest: Boolean) {

    open class Builder {
        private val TAG: String = "SecurityProvider"
        private  var path: String? = null
        private lateinit var networkClient: NetworkClient
        var createDest: Boolean = false;
        private var contextReference: WeakReference<Context>

        constructor(context: Context) {
            contextReference = WeakReference(context)
        }

        fun setNetworkClient(networkClient: NetworkClient): Builder {
            this.networkClient = networkClient
            return this
        }

        fun setOutputPath(path: String): Builder {
            this.path = path
            this.createDest = true
            return this
        }


        @Throws(MissingParameterException::class)
        fun build(): SecurityProviderOption {
            if (!::networkClient.isInitialized) {
                val error = "Network client is null"
                Log.e(TAG, error)
                throw MissingParameterException(error)
            }
            if (contextReference.get() == null) {
                throw MissingParameterException("Context is null")
            }
            path?.let{

                val file = File(it)
                if (createDest) {
                    if (!file.exists()) {
                        File(file.parent).mkdirs()
                    } else if (!file.isDirectory) {
                        throw MissingParameterException("Destination folder does not exist")
                    }
                }
                if (!file.isDirectory) {
                    throw MissingParameterException("Given destination is not a folder")
                }
            }
            return SecurityProviderOption(networkClient, contextReference, path, createDest)
        }

    }

}
