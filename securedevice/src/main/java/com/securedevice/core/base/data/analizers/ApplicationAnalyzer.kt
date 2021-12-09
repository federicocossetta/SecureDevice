package com.securedevice.core.base.data.analizers

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.securedevice.core.base.model.Application
import java.util.*

class ApplicationAnalyzer {
    private val TAG: String = "ApplicationAnalyzer"
    private var applications: MutableList<Application> = mutableListOf()
    open fun getApplication(context: Context): MutableList<Application> {
        val pm = context.packageManager
        setApplications(pm, getLaunchableApplications(pm))
        return applications
    }

    private fun setApplications(pm: PackageManager, launchableApplications: HashSet<String>) {
        for (app in pm.getInstalledApplications(0)) {
            addApplication(app!!, pm, launchableApplications)
        }
    }

    private fun getLaunchableApplications(pm: PackageManager): HashSet<String> {
        val s = HashSet<String>()
        try {
            val i = Intent(Intent.ACTION_MAIN)
            i.addCategory(Intent.CATEGORY_LAUNCHER)
            val activities = pm.queryIntentActivities(i, 0)
            for (activity in activities) {
                s.add(activity.activityInfo.packageName)
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return s
    }

    private fun addApplication(app: ApplicationInfo, pm: PackageManager, launchableApplications: HashSet<String>) {
        val pn = app.packageName
        var versionName: String? = ""
        var versionCode = -1
        var an = ""
        var launchable: Boolean? = null
        //        Boolean usesCamera
        try {
            versionName = pm.getPackageInfo(pn, 0).versionName
        } catch (e: java.lang.Exception) {

            Log.e(TAG, Log.getStackTraceString(e));
        }
        try {
            versionCode = pm.getPackageInfo(pn, 0).versionCode
        } catch (e: java.lang.Exception) {

            Log.e(TAG, Log.getStackTraceString(e));
        }
        try {
            an = pm.getApplicationLabel(app) as String
        } catch (e: java.lang.Exception) {

            Log.e(TAG, Log.getStackTraceString(e));
        }
        try {
            if (launchableApplications.contains(pn)) {
                launchable = true
            }
        } catch (e: java.lang.Exception) {

            Log.e(TAG, Log.getStackTraceString(e));
        }
        val a = Application(pn, app.sourceDir, an, versionName, versionCode, launchable)
        applications.add(a)
    }
}