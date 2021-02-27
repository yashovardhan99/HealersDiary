import dependencies.Dependencies
import dependencies.Version

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion = Version.AppVersion.compileSdk.toString()
//    buildToolsVersion "30.0.3"

    defaultConfig {
        minSdkVersion(Version.AppVersion.minSdk)
        targetSdkVersion(Version.AppVersion.targetSdk)
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(Dependencies.Android.ktx_core)
    implementation(Dependencies.Android.app_compat)
    implementation(Dependencies.Android.material)
    implementation(platform(Dependencies.Firebase.bom))
    implementation(Dependencies.Firebase.analytics)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}