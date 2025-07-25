import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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

    js(IR){
        browser()
        binaries.executable()
    }

    jvm("desktop")

    sourceSets{
        val desktopMain by getting

        androidMain.dependencies{
            implementation(libs.io.ktor.ktor.serialization.kotlinx.json3)
            implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json2)

            implementation(libs.io.ktor.ktor.client.content.negotiation)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.okhttp)
            implementation(libs.ktor.client.android)
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies{
            implementation(libs.kotlinx.datetime)
            implementation(compose.components.resources)
            implementation(libs.jetbrains.kotlinx.coroutines.core)
            implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json2)
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
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.client.cio)
            implementation(libs.io.ktor.ktor.client.content.negotiation)
            implementation(libs.io.ktor.ktor.serialization.kotlinx.json3)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
        iosMain.dependencies{
            implementation(libs.ktor.ktor.client.darwin)
            implementation(libs.io.ktor.ktor.client.content.negotiation)
            implementation(libs.io.ktor.ktor.serialization.kotlinx.json3)
            implementation(libs.ktor.ktor.client.darwin)
        }
        jsMain.dependencies{
            implementation(libs.io.ktor.ktor.client.content.negotiation)
            implementation(libs.io.ktor.ktor.serialization.kotlinx.json3)
            implementation(libs.kotlinx.coroutines.core.js)
            implementation(libs.ktor.client.js)
        }
    }
}

android{
    configurations.all {
        exclude(group="org.jetbrains.kotlin-wrappers",module="kotlin-js")
        /**uncomment this to make android work/comment out to make wasmjs work**/
        exclude(group="org.jetbrains.kotlinx",module="kotlinx-browser")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
   implementation(libs.androidx.adaptive.android)
    implementation(libs.places)
    implementation(libs.firebase.dataconnect)
    /* implementation("io.ktor:ktor-client-core:2.3.3")
       implementation("io.ktor:ktor-client-cio:2.3.3")*/
    debugImplementation(compose.uiTooling)
    implementation(libs.foundation)
    runtimeOnly(libs.ui.tooling)
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