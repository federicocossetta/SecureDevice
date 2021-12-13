-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-allowaccessmodification
#-overloadaggressively
-dontusemixedcaseclassnames
-keepparameternames
-keepattributes LineNumberTable
-dontwarn javax.annotation.**

-keep class com.securedevice.core.base.SecurityChecker { *; }
-keep class com.securedevice.core.base.SecurityChecker$GitHubApi { *; }
-keep class com.securedevice.core.base.SecurityProviderOption { *; }
-keep class com.securedevice.core.base.SecurityProviderOption$Builder { *; }
-keep class com.securedevice.core.base.model.** { *; }
-keep class com.securedevice.core.base.RepoUserCallback {*;}
-keep class com.securedevice.core.base.SecureDeviceException {*;}
-keep class com.securedevice.core.base.model.AnalysisResult {*;}
-keep class com.securedevice.core.base.data.NetworkClient {*;}
-keep class com.securedevice.core.base.data.MyNetworkCallback {*;}
-keep class com.securedevice.core.base.data.MyNetworkCallback {*;}
-keep class com.securedevice.core.base.data.NetworkSuccess {*;}
-keep class com.securedevice.core.base.data.NetworkError {*;}
-keep interface * {
   <methods>;
}
# JSR 305 annotations are for embedding nullability information.

-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

-keep @com.squareup.moshi.JsonQualifier @interface *

# Enum field names are used by the integrated EnumJsonAdapter.
# values() is synthesized by the Kotlin compiler and is used by EnumJsonAdapter indirectly
# Annotate enums with @JsonClass(generateAdapter = false) to use them with Moshi.
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}

# Keep helper method to avoid R8 optimisation that would keep all Kotlin Metadata when unwanted
-keepclassmembers class com.squareup.moshi.internal.Util {
    private static java.lang.String getKotlinMetadataClassName();
}