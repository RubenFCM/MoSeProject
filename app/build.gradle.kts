plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
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
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    val retrofitVersion = "2.11.0"
    val interceptorVersion = "4.12.0"
    val viewModelVersion = "2.7.0"

    /* RETROFIT */
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    /* INTERCEPTOR */
    implementation("com.squareup.okhttp3:okhttp:$interceptorVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$interceptorVersion")

    /*Carousel Card Slider*/
    implementation("com.google.accompanist:accompanist-pager:0.20.0")
    implementation("androidx.compose.ui:ui-util:1.6.7")

    /*Courutines*/
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    /*NavController*/
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")

    /*Descargar imaenes por URL  COIL*/
    implementation("io.coil-kt:coil-compose:2.2.2")

    /* VIEWMODEL */
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$viewModelVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$viewModelVersion")

    /*EXO*/
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")
    /* Youtube */
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:chromecast-sender:0.28")
    /* Tests */
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.20")
    testImplementation ("androidx.arch.core:core-testing:2.1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
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