import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget=JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":sharedLogic"))

    implementation("dev.icerock.moko:resources-compose:0.27.0")
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    androidTestImplementation(libs.kotlin.testJunit)
    implementation("com.kiwi.navigation-compose.typed:core:0.10.0")
    androidTestImplementation("org.jetbrains.compose.ui:ui-test:1.10.0")
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.10.0")

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace="com.example.client"
    compileSdk=libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId="com.example.client"
        minSdk=libs.versions.android.minSdk.get().toInt()
        targetSdk=libs.versions.android.targetSdk.get().toInt()
        versionCode=1
        versionName="1.0"
    }
    packaging {
        resources {
            excludes+="/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled=false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility=JavaVersion.VERSION_11
        targetCompatibility=JavaVersion.VERSION_11
    }
    buildFeatures {
        compose=true
    }
}