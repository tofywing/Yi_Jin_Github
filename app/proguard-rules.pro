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
