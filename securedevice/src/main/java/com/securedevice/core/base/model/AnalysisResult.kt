package com.securedevice.core.base.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnalysisResult(var isRooted: Boolean, var isEmulated: Boolean, var
permissions: MutableList<String>?, var deviceName: String?, var sdkVersion: Int, var packageName:
                          String?, var installedApps: MutableList<Application>?) {
    constructor() : this(false, false, null, null, 0, null, null)
}