package com.securedevice.core.base.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubUser(val login: String,  val type: String)