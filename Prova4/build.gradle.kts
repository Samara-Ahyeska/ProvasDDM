// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Declara o plugin do Kotlin Android na raiz (essencial para o módulo app usar jvmTarget/kotlinOptions)
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false

    // Plugin do Google Services para o Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
}