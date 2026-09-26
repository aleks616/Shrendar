import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidMultiplatformLibrary)
    id("dev.icerock.mobile.multiplatform-resources")
}

apply(plugin = "dev.icerock.mobile.multiplatform-resources")

multiplatformResources {
    resourcesPackage.set("com.example.client")
    resourcesClassName.set("MR")
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
            freeCompilerArgs.add("-Xexpect-actual-classes")
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
            api("dev.icerock.moko:resources:0.27.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("dev.icerock.moko:resources:0.27.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            implementation("io.ktor:ktor-client-mock:3.1.3")
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