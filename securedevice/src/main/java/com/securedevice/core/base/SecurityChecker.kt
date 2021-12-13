package com.securedevice.core.base

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

        getDeviceInfo(options.context.get()!!)

    }

    @Throws(SecurityException::class)
    open fun getGitHubApi(): GitHubApi {
        if (!isTrustedDevice()) {
            throw  SecureDeviceException("Device not trusted")
        }
        return GitHubApi(builder.networkClient)
    }

    private fun getDeviceInfo(context: Context) {
        val pm = context.packageManager
        analysisResult.permissions = PermissionsAnalyzer().getPermissions(context, pm)
        analysisResult.packageName = context.packageName
        analysisResult.sdkVersion = Build.VERSION.SDK_INT
        val deviceName = "${Build.BRAND} - ${Build.MODEL} - ${Build.MANUFACTURER}"
        analysisResult.deviceName = deviceName
        analysisResult.installedApps = ApplicationAnalyzer().getApplication(context)
        analysisResult.isRooted = EnviromentCheck.isRooted()
        analysisResult.isEmulated = EnviromentCheck.isInEmulator()

    }


    @Throws(SecurityException::class)
    open fun getAnalysisResult(): AnalysisResult {
        if (!isTrustedDevice()) {
            throw  SecureDeviceException("Device not trusted")
        }
        return analysisResult
    }


    private fun isTrustedDevice(): Boolean {
        return !analysisResult.isEmulated && !analysisResult.isRooted

    }

    class GitHubApi internal constructor(
        private val networkClient:
        NetworkClient
    ) {
        private val TAG: String = "GitHubApi"
        fun getRepoFavorites(user: String, repository: String, callback: RepoUserCallback) {

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
                        if (!usersList.isNullOrEmpty()) {
                            callback.onUserFound(usersList)
                        } else {
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