plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // si tenés alias para parcelize en el catalog y lo usás:
    // alias(libs.plugins.kotlin.parcelize) apply false
    id("com.google.dagger.hilt.android") apply false
}
