package com.securedevice.core.base.model

import com.securedevice.core.base.SecureDeviceException
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnalysisResult(
    var isRooted: Boolean,
    var isEmulated: Boolean,
    var
    permissions: MutableList<String>?,
    var deviceName: String?,
    var sdkVersion: Int,
    var packageName:
    String?,
    var installedApps: MutableList<Application>?
) {
    constructor() : this(false, false, null, null, 0, null, null)


    @Throws(SecurityException::class)
    open fun isSdkGreaterThen30(): Boolean {
        if (isRooted || isEmulated) {
            throw  SecureDeviceException("Device not trusted")
        }
        return sdkVersion >= 30
    }

    @Throws(SecurityException::class)
    open fun packageStartWith(packageName: String): Boolean {
        if (isRooted || isEmulated) {
            throw  SecureDeviceException("Device not trusted")
        }
        packageName.let { return it.startsWith(packageName) }
    }
}

