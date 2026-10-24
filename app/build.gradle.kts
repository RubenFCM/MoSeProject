plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

// Public browsing remains buildable before a Firebase project has been configured.
// Never generate replacement Firebase identifiers or a dummy google-services.json.
if (listOf("google-services.json", "src/debug/google-services.json", "src/release/google-services.json")
        .any { file(it).isFile }) {
    apply(plugin = "com.google.gms.google-services")
}

require(providers.gradleProperty("authPasswordMaxLength").orElse("4096").get().toInt() >=
    providers.gradleProperty("authPasswordMinLength").orElse("6").get().toInt()) {
    "authPasswordMaxLength must be greater than or equal to authPasswordMinLength"
}

android {
    namespace = "com.example.moseproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.moseproject"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // These are public policy settings, not credentials. Match Firebase Console before release.
        buildConfigField("int", "AUTH_PASSWORD_MIN_LENGTH", providers.gradleProperty("authPasswordMinLength").orElse("6").get().also {
            require(it.toInt() in 6..30) { "authPasswordMinLength must be between 6 and 30" }
        })
        buildConfigField("int", "AUTH_PASSWORD_MAX_LENGTH", providers.gradleProperty("authPasswordMaxLength").orElse("4096").get().also {
            require(it.toInt() in 6..4096) { "authPasswordMaxLength must be between 6 and 4096" }
        })
        listOf("LOWERCASE" to "authPasswordLowercase", "UPPERCASE" to "authPasswordUppercase",
            "NUMBER" to "authPasswordNumber", "SYMBOL" to "authPasswordSymbol").forEach { (field, property) ->
            buildConfigField("boolean", "AUTH_PASSWORD_REQUIRE_$field", providers.gradleProperty(property).orElse("false").get().also {
                require(it == "true" || it == "false") { "$property must be true or false" }
            })
        }
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "AUTH_USE_EMULATOR", providers.gradleProperty("firebaseAuthEmulator").orElse("false").get().also {
                require(it == "true" || it == "false") { "firebaseAuthEmulator must be true or false" }
            })
        }
        release {
            buildConfigField("boolean", "AUTH_USE_EMULATOR", "false")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    /* RETROFIT */
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    /*Carousel Card Slider*/
    implementation(libs.accompanist.pager)
    implementation(libs.androidx.ui.util)

    /*Courutines*/
    implementation(libs.kotlinx.coroutines.android)

    /*NavController*/
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    /*Descargar imaenes por URL  COIL*/
    implementation(libs.coil.compose)

    /* VIEWMODEL */
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    /*EXO*/
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    /* Youtube */
    implementation(libs.youtube.player)
    /* Tests */
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.androidx.arch.core.testing)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
