package dependencies

import dependencies.Dependencies.Android.Lifecycle
import dependencies.Version.Dependencies as Versions

/**
 * This is a collection of all dependencies used in the project.
 * @since v2.0.0-alpha02
 * @see Version
 * @suppress SpellCheckingInspection
 */
@Suppress("SpellCheckingInspection")
object Dependencies {
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
    const val kotlin_gradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"

    /**
     * Contains common android dependencies
     * @see Lifecycle
     * @see Room
     * @see Navigation
     * @see Hilt
     * @see Versions.Androidx
     */
    object Android {
        const val app_compat = "androidx.appcompat:appcompat:${Versions.Androidx.app_compat}"
        const val material = "com.google.android.material:material:${Versions.material}"
        const val constraint_layout =
            "androidx.constraintlayout:constraintlayout:${Versions.Androidx.constraint_layout}"
        const val recycler_view = "androidx.recyclerview:recyclerview:${Versions.Androidx.recycler}"
        const val ktx_core = "androidx.core:core-ktx:${Versions.Androidx.ktx_core}"
        const val google_shortcuts =
            "androidx.core:core-google-shortcuts:${Versions.Androidx.google_shortcuts}"
        const val multidex = "androidx.multidex:multidex:${Versions.Androidx.multidex}"
        const val datastore =
            "androidx.datastore:datastore-preferences:${Versions.Androidx.datastore}"
        const val activity_ktx = "androidx.activity:activity-ktx:${Versions.Androidx.activity}"
        const val fragment_ktx = "androidx.fragment:fragment-ktx:${Versions.Androidx.fragment}"
        const val build_gradle = "com.android.tools.build:gradle:${Versions.gradle}"
        const val workmanager = "androidx.work:work-runtime-ktx:${Versions.Androidx.workmanager}"

        /**
         * Dependencies for lifecycle methods and extensions
         * @see Versions.Androidx.lifecycle
         */
        object Lifecycle {
            const val view_model =
                "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.Androidx.lifecycle}"
            const val view_model_saved_state =
                "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.Androidx.lifecycle}"
            const val live_data =
                "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.Androidx.lifecycle}"
            const val common_java8 =
                "androidx.lifecycle:lifecycle-common-java8:${Versions.Androidx.lifecycle}"
        }
    }

    /**
     * All Firebase Dependencies
     * @see Versions.firebase_bom
     */
    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:${Versions.firebase_bom}"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        const val auth = "com.google.firebase:firebase-auth-ktx"
        const val play_services_auth =
            "com.google.android.gms:play-services-auth:${Versions.play_services_auth}"
        const val google_services = "com.google.gms:google-services:${Versions.google_services}"
        const val crashlytics_gradle =
            "com.google.firebase:firebase-crashlytics-gradle:${Versions.crashlytics}"
        const val firestore = "com.google.firebase:firebase-firestore-ktx"
        const val coroutines =
            "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.play_services_coroutines}"
    }

    /**
     * Open source licenses dependencies.
     * This is used to get a list of all OSS licenses for the dependencies used by the project.
     * @see com.google.android.gms.oss.licenses
     */
    object OssLicenses {
        const val oss_app = "com.google.android.gms:play-services-oss-licenses:${Versions.oss_app}"
        const val oss_gradle = "com.google.android.gms:oss-licenses-plugin:${Versions.oss_root}"
    }

    /**
     * Room dependencies
     * @see androidx.room
     */
    object Room {
        const val runtime = "androidx.room:room-runtime:${Versions.Androidx.room}"
        const val compiler = "androidx.room:room-compiler:${Versions.Androidx.room}"
        const val ktx = "androidx.room:room-ktx:${Versions.Androidx.room}"
        const val testing = "androidx.room:room-testing:${Versions.Androidx.room}"
    }

    /**
     * All navigation framework dependencies
     * @see Android
     * @see androidx.navigation
     */
    object Navigation {
        const val fragment =
            "androidx.navigation:navigation-fragment-ktx:${Versions.Androidx.navigation}"
        const val ui = "androidx.navigation:navigation-ui-ktx:${Versions.Androidx.navigation}"
        const val dynamic_features =
            "androidx.navigation:navigation-dynamic-features-fragment:${Versions.Androidx.navigation}"
        const val testing = "androidx.navigation:navigation-testing:${Versions.Androidx.navigation}"
        const val safe_args_plugin =
            "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.Androidx.navigation}"
    }

    /**
     * All paging 3.0 dependencies
     * @see Versions.Androidx.paging
     */
    object Paging {
        const val runtime = "androidx.paging:paging-runtime-ktx:${Versions.Androidx.paging}"
        const val common_testing = "androidx.paging:paging-common-ktx:${Versions.Androidx.paging}"
    }

    /**
     * Hilt and dagger dependencies
     * @see dagger.hilt.android
     */
    object Hilt {
        const val android = "com.google.dagger:hilt-android:${Versions.hilt_android}"
        const val android_compiler =
            "com.google.dagger:hilt-android-compiler:${Versions.hilt_android}"
        const val compiler = "androidx.hilt:hilt-compiler:${Versions.hilt}"
        const val view_model = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hilt}"
        const val gradle_plugin =
            "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt_android}"
        const val worker = "androidx.hilt:hilt-work:${Versions.hilt}"
    }
}