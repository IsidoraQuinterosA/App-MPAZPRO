plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // Genera el código de Room
    jacoco // Reporte de cobertura de las pruebas unitarias
}

android {
    namespace = "com.example.mpaz_pro_3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mpaz_pro_3"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Mide la cobertura al correr las pruebas unitarias
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        // Las clases de Android (Log, Uri...) devuelven valores por defecto en las pruebas
        unitTests.isReturnDefaultValues = true
    }
}

// Las pruebas usan JUnit 5 (Kotest corre sobre esta plataforma)
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

jacoco {
    toolVersion = "0.8.12"
}

// Reporte de cobertura: ./gradlew jacocoTestReport
// Se mide la lógica (ViewModel, Repository, validaciones y mapeos).
// Se excluye lo que no es lógica: pantallas Compose, tema, navegación, Room generado y clientes de red.
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Genera el reporte de cobertura de las pruebas unitarias"

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    val excluidos = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*_Impl*.*", "**/*Dao.*", "**/AppDatabase*.*",
        "**/ui/**", "**/navigation/**",
        "**/MainActivity*.*", "**/MpazApp*.*",
        "**/data/remote/RetrofitClient*.*", "**/data/remote/*Api.*", "**/data/remote/ApiConfig*.*",
        "**/data/preferences/**", "**/util/Archivos*.*", "**/util/ConexionInternet*.*",
        "**/ComposableSingletons*.*"
    )
    val clasesKotlin = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(excluidos) }
    classDirectories.setFrom(files(clasesKotlin))
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(layout.buildDirectory) { include("**/*.exec", "**/*.ec") })
}

dependencies {
    // Base de Android y ciclo de vida
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Interfaz con Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navegación y corrutinas
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Persistencia local: Room (base de datos) y DataStore (sesión)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Consumo de APIs REST: microservicios propios y PokeAPI
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.coil.compose)

    // Pruebas
    testImplementation(libs.junit)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.vintage.engine) // Ejecuta también las pruebas JUnit 4 antiguas
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
