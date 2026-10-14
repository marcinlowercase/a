
# Keep standard cryptography and security classes just in case R8 tries to aggressively shrink them
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }

# Preserve Google API Client and Drive models
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}
-dontwarn com.google.api.client.**
-dontwarn org.apache.http.**

-keep class org.yaml.snakeyaml.** { *; }
-keep class org.mozilla.gecko.util.DebugConfig** { *; }

# Preserve ML Kit Barcode Scanning & Common Component Registrars
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-dontwarn com.google.android.gms.internal.mlkit_vision_barcode.**

# Preserve CameraX service loaders and config
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-dontwarn androidx.camera.**