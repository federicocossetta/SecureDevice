package com.securedevice.core.base

import com.securedevice.core.base.data.NetworkError
import com.securedevice.core.base.model.GithubUser

interface RepoUserCallback : SecureDeviceExceptionCallback {
    fun onUserFound(users: List<GithubUser>)
    fun onUserNotFound()
    fun onError(error: NetworkError)
}
