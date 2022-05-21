import dependencies.Dependencies
import dependencies.Version

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = Version.AppVersion.compileSdk

    defaultConfig {
        minSdk = Version.AppVersion.minSdk
        targetSdk = Version.AppVersion.targetSdk
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }

        testBuildType = System.getProperty("testBuildType", "debug")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    testOptions {
        unitTests.isIncludeAndroidResources = true
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
    implementation(Dependencies.Hilt.worker)

    implementation(Dependencies.Android.datastore)
    implementation(Dependencies.Android.workmanager)
    implementation(Dependencies.Android.splashscreen)

    implementation(Dependencies.Room.runtime)
    kapt(Dependencies.Room.compiler)
    implementation(Dependencies.Room.ktx)
    testImplementation(Dependencies.Room.testing)
    implementation(Dependencies.Room.paging)

    implementation(Dependencies.Paging.runtime)
    testImplementation(Dependencies.Paging.common_testing)

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.2")
    testImplementation("com.google.truth.extensions:truth-java8-extension:1.1.2")
    androidTestImplementation("com.google.truth:truth:1.1.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("androidx.test:core:1.4.0")
    testImplementation("org.mockito:mockito-core:4.1.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}
