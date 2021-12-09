package com.securedevice.core.base

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import com.securedevice.core.base.data.MyNetworkCallback
import com.securedevice.core.base.data.NetworkClient
import com.securedevice.core.base.data.NetworkError
import com.securedevice.core.base.data.NetworkSuccess
import com.securedevice.core.base.data.analizers.ApplicationAnalyzer
import com.securedevice.core.base.data.analizers.PermissionsAnalyzer
import com.securedevice.core.base.model.AnalysisResult
import com.securedevice.core.base.model.GithubUser
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


open class SecurityChecker {
    private val TAG: String = "SecurityChecker"
    private var builder: SecurityProviderOption
    private var analysisResult: AnalysisResult = AnalysisResult();

    constructor(options: SecurityProviderOption) {
        builder = options
        isTrustedDevice(options)
        getDeviceInfo(options.context.get()!!)

    }

    open fun getGitHubApi(): GitHubApi {
        return GitHubApi(deviceTrusted, builder.networkClient)
    }

    private fun getDeviceInfo(context: Context) {
        val pm = context.packageManager
        analysisResult.permissions = PermissionsAnalyzer().getPermissions(context, pm)
        analysisResult.packageName = context.packageName
        analysisResult.sdkVersion = Build.VERSION.SDK_INT
        val deviceName = "${Build.BRAND} - ${Build.MODEL} - ${Build.MANUFACTURER}"
        analysisResult.deviceName = deviceName
        analysisResult.installedApps = ApplicationAnalyzer().getApplication(context)

    }

    private var deviceTrusted: Boolean = false

    @Throws(SecurityException::class)
    open fun getAnalysisResult(): AnalysisResult {
        if (!deviceTrusted) {
            throw  SecureDeviceException("Device not trusted")
        }
        return analysisResult
    }

    @Throws(SecurityException::class)
    open fun isSdkGreatherThen30(): Boolean {
        if (!deviceTrusted) {
            throw  SecureDeviceException("Device not trusted")
        }
        return analysisResult.sdkVersion >= 30
    }

    @Throws(SecurityException::class)
    open fun packageStartWith(packageName: String): Boolean {
        if (!deviceTrusted) {
            throw  SecureDeviceException("Device not trusted")
        }
        analysisResult.packageName?.let { return it.startsWith(packageName) } ?: run {
            return false
        }
    }


    private fun isTrustedDevice(options: SecurityProviderOption) {
        if (EnviromentCheck.isRooted()) {
            analysisResult.isRooted = true
        }
        if (EnviromentCheck.isInEmulator()) {
            analysisResult.isEmulated = true
        }
        var permissionRequestBuilder = PermissionUtil.PermissionRequestBuilder(
            options.context
                .get()
        )
        permissionRequestBuilder.addPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        if (builder.createDest) {
            permissionRequestBuilder.addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val build = permissionRequestBuilder.build()
        val checkPermission = PermissionUtil.checkPermission(build)
        if (!checkPermission.isSuccess) {
            throw SecureDeviceException("Missing permission")

        }
        deviceTrusted = true
    }

    inner class GitHubApi(
        private val deviceTrusted: Boolean, private val networkClient:
        NetworkClient
    ) {
        private val TAG: String = "GitHubApi"
        fun getRepoFavorites(user: String, repository: String, callback: RepoUserCallback) {
            if (!deviceTrusted) {
                callback.onDeviceUntrusted(SecureDeviceException("Device not trusted"))
                return
            }
            try {
                val baseUrl = "https://api.github.com/repos/%s/%s/stargazers"
                val finalUrl = String.format(baseUrl, user, repository)
                networkClient.makeNetworkCall(finalUrl, object : MyNetworkCallback {
                    override fun onCallSuccess(networkSuccess: NetworkSuccess) {
                        Log.d(TAG, "onCallSuccess: ")
                        val result = networkSuccess.result.toString()
                        val newParameterizedType = Types.newParameterizedType(
                            List::class.java,
                            GithubUser::class.java
                        )
                        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                        val adapter: JsonAdapter<List<GithubUser>> =
                            moshi.adapter(newParameterizedType)
                        val usersList: List<GithubUser>? = adapter.lenient().fromJson(result)
                        usersList?.let {
                            callback.onUserFound(it)
                        } ?: run {
                            callback.onUserNotFound()
                        }

                    }

                    override fun onCallError(networkError: NetworkError) {
                        callback.onError(networkError)
                        Log.d(TAG, "onCallError: ")

                    }
                })
            } catch (c: Exception) {
                Log.e(TAG, Log.getStackTraceString(c))
                callback.onError(NetworkError(c))
            }
        }
    }

}