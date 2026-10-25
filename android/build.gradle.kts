// ---------------------------------------------------------------------------
// Root build file. Plugins are declared here with `apply false` and applied in
// :app, which lets the app module apply Firebase plugins *conditionally*
// (they hard-fail when google-services.json is missing).
// ---------------------------------------------------------------------------
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
