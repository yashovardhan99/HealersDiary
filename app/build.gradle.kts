import dependencies.Dependencies
import dependencies.Version

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
}

val buildParam = buildscript.toString()
// Needed for CI
if (buildParam != "dev") {
    //exclude production build
    android {
        variantFilter {
            if (buildType.name == "dev") {
                ignore = true
            }
        }
    }
} else {
    //exclude all except production build
    android {
        variantFilter {
            if (buildType.name != "dev") {
                ignore = true
            }
        }
    }
}
android {
    compileSdkVersion = Version.AppVersion.compileSdk.toString()
    defaultConfig {
        applicationId = "com.yashovardhan99.healersdiary"
        minSdkVersion(Version.AppVersion.minSdk)
        targetSdkVersion(Version.AppVersion.targetSdk)
        versionCode = Version.AppVersion.versionCode
        versionName = Version.AppVersion.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
    buildTypes {
        getByName("debug") {
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            minifyEnabled(false)
        }
        create("dev") { //needed for CI
            minifyEnabled(true)
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            minifyEnabled(true)
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    lintOptions {
        isAbortOnError = false
    }
    tasks.register("generateDependencyListFile") {
        configurations.getByName("implementation").setCanBeResolved(true)
        configurations.getByName("androidTestImplementation").setCanBeResolved(true)
        configurations.getByName("api").setCanBeResolved(true)

        doLast {
            var str = "# auto-generated from ${this.name}; this file should be checked into version control\n"
            val resolvedImplementationConfig = configurations.implementation.resolvedConfiguration
            val resolvedAndroidTestImplementationConfig = configurations.androidTestImplementation.resolvedConfiguration
            val resolvedApiConfig = configurations.api.resolvedConfiguration

            resolvedImplementationConfig.firstLevelModuleDependencies.onEach { dep ->
                str += "${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}\n"
            }

            resolvedAndroidTestImplementationConfig.firstLevelModuleDependencies.onEach { dep ->
                str += "${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}\n"
            }

            resolvedApiConfig.firstLevelModuleDependencies.onEach { dep ->
                str += "${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}\n"
            }

            File(projectDir, "deps.list.txt").writeText(str)
        }
    }
    tasks.getByName("preBuild").dependsOn(tasks.getByName("generateDependencyListFile"))
    dynamicFeatures = mutableSetOf(":online")
}
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(Dependencies.Android.app_compat)
    api(Dependencies.Android.material)
    implementation(Dependencies.Android.constraint_layout)
    implementation(Dependencies.Android.recycler_view)
    implementation(Dependencies.Android.ktx_core)
    implementation(Dependencies.Android.activity_ktx)
    implementation(Dependencies.Android.fragment_ktx)
    implementation(Dependencies.Android.multidex)
    api(Dependencies.Android.datastore)
    implementation(Dependencies.Android.workmanager)

    implementation(Dependencies.Android.Lifecycle.view_model)
    implementation(Dependencies.Android.Lifecycle.view_model_saved_state)
    implementation(Dependencies.Android.Lifecycle.live_data)
    implementation(Dependencies.Android.Lifecycle.common_java8)

    implementation(platform(Dependencies.Firebase.bom))
    implementation(Dependencies.Firebase.analytics)
    implementation(Dependencies.Firebase.crashlytics)
    implementation(Dependencies.Firebase.auth)
    implementation(Dependencies.Firebase.play_services_auth)
    implementation(Dependencies.Firebase.firestore)

    implementation(Dependencies.OssLicenses.oss_app)

    implementation(Dependencies.Room.runtime)
    kapt(Dependencies.Room.compiler)
    implementation(Dependencies.Room.ktx)
    testImplementation(Dependencies.Room.testing)

    implementation(Dependencies.Navigation.fragment)
    implementation(Dependencies.Navigation.ui)
    implementation(Dependencies.Navigation.dynamic_features)
    androidTestImplementation(Dependencies.Navigation.testing)

    implementation(Dependencies.Paging.runtime)
    testImplementation(Dependencies.Paging.common_testing)

    api(Dependencies.Hilt.android)
    kapt(Dependencies.Hilt.android_compiler)
    kapt(Dependencies.Hilt.compiler)
    api(Dependencies.Hilt.view_model)

    api(Dependencies.timber)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.1")
}