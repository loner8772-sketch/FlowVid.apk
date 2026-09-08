# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Room entities and DAOs are referenced via generated code; keep annotations.
-keepattributes *Annotation*
-keep class com.flowvid.app.data.local.** { *; }

# Media3 / ExoPlayer keeps its own consumer rules; nothing extra required here.

# Kotlin coroutines / metadata
-keepattributes Signature
-keepattributes InnerClasses
-dontwarn kotlinx.coroutines.**
