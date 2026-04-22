plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.inprideexchange"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.inprideexchange"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.foundation.layout)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil3.compose)
    implementation(libs.coil3.network.okhttp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)





    // for constrain layout ...
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.1.0")

    // extended material icons
    implementation("androidx.compose.material:material-icons-extended")

    // DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.1.1")


    implementation("androidx.navigation:navigation-compose:2.8.3")


    implementation("com.google.accompanist:accompanist-navigation-animation:0.36.0")

    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    implementation ("androidx.compose.animation:animation-core:1.5.0")
    implementation ("androidx.compose.animation:animation:1.5.0")


    // for image
    implementation("io.coil-kt:coil-compose:2.0.0-rc01")

    implementation ("com.github.bumptech.glide:compose:1.0.0-alpha.1")

    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")

    // for live data
    implementation ("androidx.compose.runtime:runtime-livedata")




    ////  Text dependency
    implementation("androidx.compose.ui:ui-text")





    // Biometric auth
    implementation("androidx.biometric:biometric:1.2.0-alpha03")


    // Network requests (OkHttp)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.tbuonomo:dotsindicator:5.0")



    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.7.1")
    implementation("com.squareup.retrofit2:converter-gson:2.6.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.5.0")

    /// ecrypted shared prefence
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")

    // for bottom sheet
    implementation ("androidx.compose.material3:material3:1.2.1")



    // for splash screen
    implementation ("androidx.core:core-splashscreen:1.0.1")


    // special case for material 3
    implementation("com.google.android.material:material:1.12.0")

    val media3_version = "1.3.1"
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    implementation("androidx.media3:media3-ui:$media3_version")


}
