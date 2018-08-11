# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\Android\Sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#1.基本指令区
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
#-ignorewarning
# 混淆前后的映射
-printmapping mapping.txt
# apk 包内所有 class 的内部结构
-dump class_files.txt
# 未混淆的类和成员
-printseeds seeds.txt
# 列出从 apk 中删除的代码
-printusage unused.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

#2.默认保留区
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}

#3.webview
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.webView, jav.lang.String);
}

#4jar包
#android-logging-log4j-1.0.3.jar
-keep class de.mindpipe.android.logging.log4j.** { *; }
#log4j-1.2.17.jar
-dontwarn org.apache.log4j.**
-keep class org.apache.log4j.** { *; }

#5complie包
#com.belerweb:pinyin4j:2.5.0
-keep class com.hp.hpl.sparta.** { *; }
-keep class net.sourceforge.pinyin4j.** { *; }

#com.squareup.okhttp3:okhttp:3.3.1
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

#junit:junit:4.12
-keep class junit.** { *; }
-keep class org.junit.** { *; }

#com.github.zhangliangming:SwipeBackLayout、com.github.zhangliangming:RotateLayout
#com.github.zhangliangming:SeekBar
-keep class com.zlm.libs.widget.** { *; }

#com.github.zhangliangming:HPLyrics
-keep class com.zlm.hp.lyrics.** { *; }

#com.github.zhangliangming:HPAudio
-dontwarn javax.**
-dontwarn java.awt.**
-keep class org.jaudiotagger.** { *; }
-keep class davaguine.jmac.** { *; }
-keep class com.wavpack.** { *; }
-keep class com.zlm.hp.audio.** { *; }

#com.github.zhangliangming:Register:v1.0
-keep class com.zlm.libs.register.** { *; }

#6混淆项目代码
-keep class com.zlm.hp.**
-keepclassmembers class com.zlm.hp.** {
   public *;
}
-keep class tv.danmaku.ijk.media.player.** { *; }

#leakcanary 内存泄露
-keep public class com.squareup.leakcanary.**{*;}

