import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "marcinlowercase.a"
    compileSdk = 36

    ndkVersion = "29.0.14206865"
    defaultConfig {
        applicationId = "marcinlowercase.a"
        minSdk = 29
        targetSdk = 36
        versionCode = 15
        versionName = "zero[15]"

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


    buildTypes {
        release {
            // Enables code-related app optimization.
            isMinifyEnabled = true
            // Enables resource shrinking.
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),

            )
            ndk {
                debugSymbolLevel = "FULL"
            }

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    kotlin {
        target {
            compilerOptions {
                optIn.add("kotlin.RequiresOptIn")
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }
    }
}

dependencies {
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)

}