package com.securedevice.core.base

import android.opengl.GLES20
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.*

internal class EnviromentCheck {
    private var rootPath: String? = null

    companion object {
        val TAG = "RootUtil"
        fun isRooted(): Boolean {
            val enviromentCheck = EnviromentCheck()
            var rooted = enviromentCheck.getRootPath() != null && enviromentCheck.getRootPath()!!
                .isNotEmpty()
            if (!rooted) {
                rooted = ExecShell()
                    .executeCommand(SHELL_CMD.check_su_binary) != null
            }
            Log.d(TAG, "rooted $rooted")
            return rooted
        }

        fun isInEmulator(): Boolean {
            var ratingCheckEmulator = 0
            if (Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains("Andy") || Build.PRODUCT.contains(
                    "ttVM_Hdragon"
                ) ||
                Build.PRODUCT.contains("google_sdk") || Build.PRODUCT.contains("Droid4X") || Build.PRODUCT.contains(
                    "nox"
                ) ||
                Build.PRODUCT.contains("sdk_x86") || Build.PRODUCT.contains("sdk_google") || Build.PRODUCT.contains(
                    "vbox86p"
                )
            ) {
                ratingCheckEmulator++
            }
            if (Build.MANUFACTURER == "unknown" || Build.MANUFACTURER == "Genymotion" || Build.MANUFACTURER.contains(
                    "Andy"
                ) ||
                Build.MANUFACTURER.contains("MIT") || Build.MANUFACTURER.contains("nox") || Build.MANUFACTURER.contains(
                    "TiantianVM"
                )
            ) {
                ratingCheckEmulator++
            }
            if (Build.BRAND == "generic" || Build.BRAND == "generic_x86" || Build.BRAND == "TTVM" ||
                Build.BRAND.contains("Andy")
            ) {
                ratingCheckEmulator++
            }
            if (Build.DEVICE.contains("generic") || Build.DEVICE.contains("generic_x86") || Build.DEVICE.contains(
                    "Andy"
                ) ||
                Build.DEVICE.contains("ttVM_Hdragon") || Build.DEVICE.contains("Droid4X") || Build.DEVICE.contains(
                    "nox"
                ) ||
                Build.DEVICE.contains("generic_x86_64") || Build.DEVICE.contains("vbox86p")
            ) {
                ratingCheckEmulator++
            }
            if (Build.MODEL == "sdk" || Build.MODEL == "google_sdk" || Build.MODEL.contains("Droid4X") ||
                Build.MODEL.contains("TiantianVM") || Build.MODEL.contains("Andy") || Build.MODEL == "Android SDK built for x86_64" || Build.MODEL == "Android SDK built for x86"
            ) {
                ratingCheckEmulator++
            }
            if (Build.HARDWARE == "goldfish" || Build.HARDWARE == "vbox86" || Build.HARDWARE.contains(
                    "nox"
                ) ||
                Build.HARDWARE.contains("ttVM_x86")
            ) {
                ratingCheckEmulator++
            }
            if (Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains("generic/sdk/generic") ||
                Build.FINGERPRINT.contains("generic_x86/sdk_x86/generic_x86") || Build.FINGERPRINT.contains(
                    "Andy"
                ) ||
                Build.FINGERPRINT.contains("ttVM_Hdragon") || Build.FINGERPRINT.contains("generic_x86_64") ||
                Build.FINGERPRINT.contains("generic/google_sdk/generic") || Build.FINGERPRINT.contains(
                    "vbox86p"
                ) ||
                Build.FINGERPRINT.contains("generic/vbox86p/vbox86p")
            ) {
                ratingCheckEmulator++
            }
            try {
                val opengl = GLES20.glGetString(GLES20.GL_RENDERER)
                if (opengl != null) {
                    if (opengl.contains("Bluestacks") || opengl.contains("Translator")) {
                        ratingCheckEmulator += 10
                    }
                }
            } catch (ignored: Exception) {
            }
            try {
                val sharedFolder = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separatorChar + "windows" + File.separatorChar +
                            "BstSharedFolder"
                )
                if (sharedFolder.exists()) {
                    ratingCheckEmulator += 10
                }
            } catch (ignored: Exception) {
            }
            val isInEmulator = ratingCheckEmulator > 3
            Log.w(
                TAG,
                String.format(
                    "isInEmulator=%s ratingCheckEmulator=%s",
                    isInEmulator,
                    ratingCheckEmulator
                )
            )
            return isInEmulator
        }

    }


    private fun getRootPath(): String? {
        try {
            if (rootPath != null) {
                return rootPath
            }
            if (checkBinaryPaths()) {
                return rootPath
            }
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
        rootPath = ""
        return rootPath
    }

    private fun checkBinaryPaths(): Boolean {
        return try {
            val paths = arrayOf(
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su"
            )
            for (path in paths) {
                if (File(path).exists()) {
                    Log.d(TAG, "su binary found at$path")
                    rootPath = path
                    return true
                }
            }
            Log.w(TAG, "No su binary found")
            false
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            false
        }
    }


    enum class SHELL_CMD(var command: Array<String>) {
        check_su_binary(
            arrayOf(
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su"
            )
        );
    }

    class ExecShell {
        fun executeCommand(shellCmd: SHELL_CMD): ArrayList<String?>? {
            Log.d(TAG, "executeCommand: ")
            var line: String? = null
            val fullResponse = ArrayList<String?>()
            var localProcess: Process? = null
            try {
                localProcess = Runtime.getRuntime().exec(shellCmd.command)
            } catch (e: Exception) {
                return null
            }
            val out = BufferedWriter(
                OutputStreamWriter(
                    localProcess!!.outputStream
                )
            )
            val `in` = BufferedReader(
                InputStreamReader(
                    localProcess!!.inputStream
                )
            )
            try {
                while (`in`.readLine().also { line = it } != null) {
                    Log.d(TAG, "--> Line received: $line")
                    fullResponse.add(line)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d(TAG, "--> Full response was: $fullResponse")
            return fullResponse
        }
    }
}