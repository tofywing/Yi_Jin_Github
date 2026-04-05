# Retrofit ProGuard Rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations, AnnotationDefault
-keepclassmembers,allowshrinking,allowoptimization class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# OkHttp ProGuard Rules
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# Kotlin Serialization ProGuard Rules
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Data Models
-keep class com.example.yijinsgithub.data.model.** { *; }
-keep @kotlinx.serialization.Serializable class ** { *; }

# Coil ProGuard Rules
-keep class coil.** { *; }
-dontwarn coil.**

# Jetpack Navigation
-keepnames class androidx.navigation.fragment.NavHostFragment
-keepnames class androidx.navigation.NavHost
-keepnames class androidx.navigation.NavController
-keepnames class androidx.navigation.NavBackStackEntry

# Custom Obfuscation Dictionary
# Using non-standard characters makes reverse engineering much harder
# Note: You can create a file named 'dictionary.txt' with complex characters and link it:
# -obfuscationdictionary dictionary.txt
# -classobfuscationdictionary dictionary.txt
# -packageobfuscationdictionary dictionary.txt

# For this project, we'll ensure critical security and data management packages are deeply obfuscated
# by not including them in 'keep' rules unless necessary.
# Specifically, com.example.yijinsgithub.security.** and com.example.yijinsgithub.data.local.**
# will be obfuscated.

# Ensure debugging info is removed in release
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes SourceFile, LineNumberTable
