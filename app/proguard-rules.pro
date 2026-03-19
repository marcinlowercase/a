# --- WebAPK Builder / ApkSigner Rules ---
# Keep the entire apksig library intact so it doesn't crash during runtime signing in Release mode
-keep class com.android.apksig.** { *; }
-dontwarn com.android.apksig.**

# Keep standard cryptography and security classes just in case R8 tries to aggressively shrink them
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }