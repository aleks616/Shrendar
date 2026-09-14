import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {iosTarget->
        iosTarget.binaries.framework {
            baseName="SharedLogic"
            isStatic=true
        }
    }

    js {
        outputModuleName="sharedLogic"
        browser()
        binaries.library()
        generateTypeScriptDefinitions()
        compilerOptions {
            target="es2015"
            optIn.add("kotlin.js.ExperimentalJsExport")
        }
    }

    android {
        namespace="com.example.client.sharedLogic"
        compileSdk=libs.versions.android.compileSdk.get().toInt()
        minSdk=libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget=JvmTarget.JVM_11
        }
        androidResources {
            enable=true
        }
        withHostTest {
            isIncludeAndroidResources=true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation(libs.ktor.client.js)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}