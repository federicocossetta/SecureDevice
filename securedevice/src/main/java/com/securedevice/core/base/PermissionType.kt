package com.securedevice.core.base

internal enum class PermissionType(val ext: String) {
    ALL_MANDATORY("&"), AT_LEAST_ONCE("|");

}