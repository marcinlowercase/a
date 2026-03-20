plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "marcinlowercase.a"
    compileSdk = 36

    ndkVersion = "29.0.14206865"
    defaultConfig {
        minSdk = 29 // android 10
        targetSdk = 36
        versionCode = 25
        versionName = "0.12.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            debugSymbolLevel = "FULL"
        }
        packaging {
            jniLibs {
                keepDebugSymbols.add("**/lib*.so")
            }
        }


    }


    // 1. Define the dimensions
    flavorDimensions.add("distribution")

    // 2. Define the versions
    productFlavors {
        create("foss") { // The GitHub / Open Source version
            dimension = "distribution"
            applicationId = "marcinlowercase.a"
            versionNameSuffix = ".a"
//            manifestPlaceholders["appLabel"] = ".a"
        }

        create("playstore") { // The Google Play version
            dimension = "distribution"
            applicationId = "studio.oo1.browser"
//             manifestPlaceholders["appLabel"] = "browser"
        }
    }

    buildTypes {
        release {
            // Enables code-related app optimization.
            isMinifyEnabled = true
            // Enables resource shrinking.
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }

        }
    }

    androidResources {
        noCompress += listOf("apk", "pk8", "der")
    }

    splits {
        abi {
            isEnable = true
            reset()
            // INCLUDE these two architectures
            include("arm64-v8a")
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.android.apksig)
    implementation(libs.androidx.media)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.androidx.compose.material3)
    implementation(libs.mozilla.geckoview)
    implementation(libs.androidx.compose.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)

}