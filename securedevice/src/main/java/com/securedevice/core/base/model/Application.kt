package com.securedevice.core.base.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Application(private val pn: String, private val path: String, private val an: String, private val versionName: String?, private val versionCode: Int, private val launchable: Boolean?,
                           )