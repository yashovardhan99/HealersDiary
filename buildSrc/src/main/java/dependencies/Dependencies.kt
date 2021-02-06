package dependencies

import dependencies.Version.Dependencies as Versions

@Suppress("SpellCheckingInspection")
object Dependencies {
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
    const val kotlin_gradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"

    object Android {
        const val app_compat = "androidx.appcompat:appcompat:${Versions.Androidx.app_compat}"
        const val material = "com.google.android.material:material:${Versions.material}"
        const val constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.Androidx.constraint_layout}"
        const val recycler_view = "androidx.recyclerview:recyclerview:${Versions.Androidx.recycler}"
        const val ktx_core = "androidx.core:core-ktx:${Versions.Androidx.ktx_core}"
        const val multidex = "androidx.multidex:multidex:${Versions.Androidx.multidex}"
        const val datastore = "androidx.datastore:datastore-preferences:${Versions.Androidx.datastore}"
        const val activity_ktx = "androidx.activity:activity-ktx:${Versions.Androidx.activity}"
        const val fragment_ktx = "androidx.fragment:fragment-ktx:${Versions.Androidx.fragment}"
        const val build_gradle = "com.android.tools.build:gradle:${Versions.gradle}"
        const val workmanager = "androidx.work:work-runtime-ktx:${Versions.Androidx.workmanager}"

        object Lifecycle {
            const val extensions = "androidx.lifecycle:lifecycle-extensions:${Versions.Androidx.lifecycle}"
            const val view_model = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.Androidx.lifecycle}"
            const val view_model_saved_state = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.Androidx.lifecycle}"
            const val live_data = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.Androidx.lifecycle}"
            const val common_java8 = "androidx.lifecycle:lifecycle-common-java8:${Versions.Androidx.lifecycle}"
        }
    }

    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:${Versions.firebase_bom}"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        const val auth = "com.google.firebase:firebase-auth-ktx"
        const val play_services_auth = "com.google.android.gms:play-services-auth:${Versions.play_services_auth}"
        const val google_services = "com.google.gms:google-services:${Versions.google_services}"
        const val crashlytics_gradle = "com.google.firebase:firebase-crashlytics-gradle:${Versions.crashlytics}"
        const val firestore = "com.google.firebase:firebase-firestore-ktx"
    }

    object OssLicenses {
        const val oss_app = "com.google.android.gms:play-services-oss-licenses:${Versions.oss_app}"
        const val oss_gradle = "com.google.android.gms:oss-licenses-plugin:${Versions.oss_root}"
    }

    object Room {
        const val runtime = "androidx.room:room-runtime:${Versions.Androidx.room}"
        const val compiler = "androidx.room:room-compiler:${Versions.Androidx.room}"
        const val ktx = "androidx.room:room-ktx:${Versions.Androidx.room}"
        const val testing = "androidx.room:room-testing:${Versions.Androidx.room}"
    }

    object Navigation {
        const val fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.Androidx.navigation}"
        const val ui = "androidx.navigation:navigation-ui-ktx:${Versions.Androidx.navigation}"
        const val dynamic_features = "androidx.navigation:navigation-dynamic-features-fragment:${Versions.Androidx.navigation}"
        const val testing = "androidx.navigation:navigation-testing:${Versions.Androidx.navigation}"
        const val safe_args_plugin = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.Androidx.navigation}"
    }

    object Paging {
        const val runtime = "androidx.paging:paging-runtime-ktx:${Versions.Androidx.paging}"
        const val common_testing = "androidx.paging:paging-common-ktx:${Versions.Androidx.paging}"
    }

    object Hilt {
        const val android = "com.google.dagger:hilt-android:${Versions.hilt_android}"
        const val android_compiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt_android}"
        const val compiler = "androidx.hilt:hilt-compiler:${Versions.hilt}"
        const val view_model = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hilt}"
        const val gradle_plugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt_android}"
    }
}