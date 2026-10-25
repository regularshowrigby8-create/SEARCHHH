# ---------------------------------------------------------------------------
# R8 rules for release builds
# ---------------------------------------------------------------------------

# --- kotlinx.serialization -------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.searchhh.app.**$$serializer { *; }
-keepclassmembers class com.searchhh.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.searchhh.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Retrofit / OkHttp -----------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn kotlinx.**
-keepattributes Signature, Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# --- Room ------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Firebase / Sentry -----------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# --- Tink (security-crypto) ------------------------------------------------
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.crypto.tink.** { *; }
