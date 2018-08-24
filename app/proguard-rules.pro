# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/sant/android-sdk-linux/tools/proguard/proguard-android.txt
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

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okhttp
-dontwarn com.android.okhttp.**
-keep class com.android.okhttp.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}

-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

-keep class com.hai.store.http.**{*;}
-keep class com.hai.store.bean.**{*;}
-keep class com.hai.store.data.DownloadCart.**{*;}
-keep class com.hai.store.view.**{*;}
-keep class com.hai.store.Application {*;}

#### -- Picasso --
-dontwarn com.squareup.picasso.**

-keep class com.hai.store.keepalive.**{*;}

-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.** { *; }


-printmapping mapping.txt
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-ignorewarnings