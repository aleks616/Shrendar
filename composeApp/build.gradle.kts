import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins{
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.4.21"
}

repositories{
    mavenCentral()
    google()
}

kotlin{
    androidTarget{
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions{
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach{iosTarget ->
        iosTarget.binaries.framework{
            baseName="ComposeApp"
            isStatic=true
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs{
        dependencies{
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.16.0")
            implementation("org.jetbrains.kotlin-wrappers:kotlin-js:1.0.0-pre.619")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
//            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-wasm:1.8.0")
//            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-wasm:1.6.3")
        }
        moduleName="composeApp"
        browser()
        binaries.executable()
    }

    sourceSets{
        val desktopMain by getting

        androidMain.dependencies{
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
            implementation("io.ktor:ktor-client-okhttp:2.3.12")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("io.ktor:ktor-client-android:2.3.12")
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies{
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
/*            implementation("io.ktor:ktor-client-core:2.3.3") {
                exclude(group="org.jetbrains.kotlin",module="kotlin-stdlib")
            }
            implementation("org.jetbrains.kotlin-wrappers:kotlin-js:1.0.0-pre.619")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("io.ktor:ktor-client-cio:2.3.3")
            implementation(compose.runtime)*/
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
        desktopMain.dependencies{
            implementation("io.ktor:ktor-client-java:2.3.12")
            implementation("io.ktor:ktor-client-cio:2.3.12")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
        iosMain.dependencies{
            implementation("io.ktor:ktor-client-darwin:2.3.12")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
            implementation("io.ktor:ktor-client-darwin:2.3.12")
        }
    }
}

android{
    configurations.all {
        exclude(group="org.jetbrains.kotlin-wrappers",module="kotlin-js")
        /**uncomment this to make android work/comment out to make wasmjs work**/
        //exclude(group="org.jetbrains.kotlinx",module="kotlinx-browser")
    }
    apply(plugin="com.android.application")
    namespace="org.aleks616.shrendar"
    compileSdk=libs.versions.android.compileSdk.get().toInt()

    defaultConfig{
        applicationId="org.aleks616.shrendar"
        minSdk=libs.versions.android.minSdk.get().toInt()
        targetSdk=libs.versions.android.targetSdk.get().toInt()
        versionCode=1
        versionName="1.0"
    }
    packaging{
        resources.excludes += setOf(
            "META-INF/versions/9/module-info.class",
            "**/kotlinx-browser*",
            "**/kotlin-js*"
        )
        resources.excludes+="**/kotlinx-browser*.module"
        resources{
            excludes+="/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes{
        getByName("release"){
            isMinifyEnabled=false
        }
    }
    compileOptions{
        sourceCompatibility=JavaVersion.VERSION_11
        targetCompatibility=JavaVersion.VERSION_11
    }
}

dependencies{
   implementation(libs.androidx.adaptive.android)
    implementation(project(":composeApp"))
    /* implementation("io.ktor:ktor-client-core:2.3.3")
       implementation("io.ktor:ktor-client-cio:2.3.3")*/
    debugImplementation(compose.uiTooling)
    implementation("org.jetbrains.compose.foundation:foundation:1.5.0")
    runtimeOnly("org.jetbrains.compose.ui:ui-tooling:1.5.0")
}

compose.desktop{
    application{
        mainClass="org.aleks616.shrendar.MainKt"

        nativeDistributions{
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName="org.aleks616.shrendar"
            packageVersion="1.0.0"
        }
    }
    dependencies{
      /*  implementation("io.ktor:ktor-client-core:2.3.3")
        implementation("io.ktor:ktor-client-cio:2.3.3")*/
    }
}