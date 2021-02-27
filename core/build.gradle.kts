import dependencies.Dependencies
import dependencies.Version

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(Version.AppVersion.compileSdk)
//    buildToolsVersion "30.0.3"

    defaultConfig {
        minSdkVersion(Version.AppVersion.minSdk)
        targetSdkVersion(Version.AppVersion.targetSdk)
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("dev") {
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(Dependencies.Android.ktx_core)
    implementation(Dependencies.Android.app_compat)
    implementation(Dependencies.Android.material)

    implementation(platform(Dependencies.Firebase.bom))
    implementation(Dependencies.Firebase.analytics)

    implementation(Dependencies.timber)

    implementation(Dependencies.Hilt.android)
    kapt(Dependencies.Hilt.android_compiler)
    kapt(Dependencies.Hilt.compiler)
    implementation(Dependencies.Hilt.view_model)

    implementation(Dependencies.Android.datastore)

    implementation(Dependencies.Room.runtime)
    kapt(Dependencies.Room.compiler)
    implementation(Dependencies.Room.ktx)
    testImplementation(Dependencies.Room.testing)

    implementation(Dependencies.Paging.runtime)
    testImplementation(Dependencies.Paging.common_testing)

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.2")
    testImplementation("com.google.truth.extensions:truth-java8-extension:1.1.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.1")
}