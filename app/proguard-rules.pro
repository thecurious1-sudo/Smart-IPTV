# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes Annotation,InnerClasses,EnclosingMethod
-dontoptimize
-dontwarn com.squareup.**.**
-dontwarn com.squareup.picasso.**
-dontwarn org.apache.**
-dontwarn junit.**.**
-dontwarn  okio.**
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.base.**
-dontwarn com.google.common.collect.**
-dontwarn org.fourthline.cling.**
-dontwarn org.seamless.**
-dontwarn org.eclipse.jetty.**
-keep class vaibhav.tech.smartiptv.cast.CastOptionsProvider { *; }
-keep class android.support.** { *; }
-keep class com.google.** { *; }
-keep class java.nio.file.** { *; }
-keep class org.apache.** { *; }
-keep class com.github.riccardove.** { *; }
-keep class de.mindpipe.** { *; }
-keep class org.nanohttpd.** { *; }
-keep class log4j.** { *; }
-keep class com.github.albfernandez.** { *; }
-keep class com.crashlytics.** { *; }
-keep class com.googlecode.** { *; }
-keep class org.jsoup.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class org.fourthline.cling.** {*;}
-keep class org.seamless.** {*;}
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
-ignorewarnings
-keep class * {
    public private *;
}