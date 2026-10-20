// Archivo de configuración principal. Aquí solo se declaran los plugins;
// cada módulo (en este caso :app) decide cuáles aplicar.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
