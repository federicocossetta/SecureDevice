package com.securedevice.core.base

import com.securedevice.core.base.SecureDeviceException

interface SecureDeviceExceptionCallback {
    fun onDeviceUntrusted(exception: SecureDeviceException)
}
