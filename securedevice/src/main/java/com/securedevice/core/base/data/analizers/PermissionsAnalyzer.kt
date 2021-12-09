package com.securedevice.core.base.data.analizers

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.PermissionChecker

open class PermissionsAnalyzer {

    private var permissions: MutableList<String> = mutableListOf()
    private val TAG: String = "PermissionsAnalyzer"


    open fun getPermissions(context: Context, pm: PackageManager):MutableList<String> {
        try {
            val permissions: Array<String?>? = retrieveManifestPermissions(pm, context.packageName)
            permissions?.let {
                for (permission in it) {
                    if (PermissionChecker.checkSelfPermission(context, permission!!) ==
                            PermissionChecker.PERMISSION_GRANTED) {
                        addPermission(permission!!)
                    }
                }
            }

        } catch (e: java.lang.Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
        return permissions
    }

    private fun retrieveManifestPermissions(pm: PackageManager, packageName: String): Array<String?>? {
        var permissions: Array<String?>? = null
        try {
            permissions = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
        } catch (e: java.lang.Exception) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        if (permissions == null) {
            permissions = arrayOf()
        }
        return permissions
    }

    private fun addPermission(permission: String) {
        permissions.add(permission)
    }

}