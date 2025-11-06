import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

android {
    signingConfigs {
        create("release") {
            val signingProperties = Properties()
            signingProperties.load(project.rootProject.file("signing.properties").reader())
            storeFile = file(signingProperties.getProperty("storeFile"))
            storePassword = signingProperties.getProperty("storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
        }
    }
    namespace = "com.ltxhhz.where_is_my_file"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ltxhhz.where_is_my_file"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.1.0"

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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    applicationVariants.all {
        outputs.all {
            val ver = defaultConfig.versionName
            val minSdk =
                project.extensions.getByType(BaseAppModuleExtension::class.java).defaultConfig.minSdk
            val abi = filters.find { it.filterType == "ABI" }?.identifier ?: "all"
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "${project.name}-$ver-${abi}-sdk$minSdk.apk"
            println("Output File Dir: " + outputFile.parent)
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.compose.material.icons.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.xxpermissions)

    // 添加 Compose 依赖
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material3)
}
