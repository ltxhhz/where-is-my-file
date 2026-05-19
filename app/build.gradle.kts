import java.util.Properties
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.impl.dirName

val signingPropertiesFile = project.rootProject.file("signing.properties")
val hasReleaseSigning = signingPropertiesFile.exists()

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
}

configure<ApplicationExtension> {
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
            val signingProperties = Properties()
            signingProperties.load(signingPropertiesFile.reader())
            storeFile = file(signingProperties.getProperty("storeFile"))
            storePassword = signingProperties.getProperty("storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
            }
        }
    }
    namespace = "com.ltxhhz.where_is_my_file"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ltxhhz.where_is_my_file"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
            resValue("string", "app_name", "@string/app_name_debug")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    kotlin {
//        jvmToolchain(17)
//    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
        resValues = true
    }

}

androidComponents {
    onVariants { variant ->
        val minSdk = variant.minSdk.apiLevel
        variant.outputs.forEach { output ->
            val ver = output.versionName.get()
            // 获取ABI信息
            val abiFilter = output.filters.find {
                it.filterType == com.android.build.api.variant.FilterConfiguration.FilterType.ABI
            }
            val abi = abiFilter?.identifier ?: "all"

            output.outputFileName.set(
                "${variant.name}-$ver-${abi}-sdk$minSdk.apk"
            )
            val outputDir =
                layout.buildDirectory.dir("outputs/apk/${variant.name}")
            println("Output File Dir: " + outputDir.get().asFile.absolutePath)
            println("Output File Name: " + output.outputFileName.get())
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.compose.material.icons.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 添加 Compose 依赖
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material3)
}
