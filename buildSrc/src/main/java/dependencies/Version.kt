package dependencies

import dependencies.Version.Dependencies
import java.text.NumberFormat

/**
 * A repository of Versions used by the project
 * @see Dependencies
 */
object Version {

    /**
     * App versions such as minSdk, version code etc.
     */
    object AppVersion {
        const val minSdk = 21
        const val targetSdk = 31
        const val compileSdk = 31
        private const val major = 2
        private const val minor = 0
        private const val patch = 0

        private val buildType: BuildType = BuildType.BETA
        private const val buildCode = 4

        private const val standard = (major * 100 + minor) * 100 + patch
        val versionCode = (standard * 10 + buildType.typeCode) * 100 + buildCode
        val versionName = buildVersionName()

        /**
         * Builds a version name string such as "2.0.1-beta-01"
         */
        private fun buildVersionName(): String {
            val builder = StringBuilder("$major.$minor.$patch")
            return when {
                buildType is BuildType.RELEASE -> builder.toString()
                buildCode == 0 -> builder.append(builder.append("-${buildType.typeString}"))
                    .toString()
                else -> {
                    val code = NumberFormat.getInstance().apply { minimumIntegerDigits = 2 }
                        .format(buildCode)
                    builder.append("-${buildType.typeString}").append(code).toString()
                }
            }
        }

        /**
         * A sealed class for all build types
         * @param typeCode Integer between 0 and 3 representing different types
         * @param typeString String representing different types
         */
        @Suppress("unused")
        private sealed class BuildType(val typeCode: Int, val typeString: String) {
            object ALPHA : BuildType(0, "alpha")
            object BETA : BuildType(1, "beta")
            object RC : BuildType(2, "rc")
            object RELEASE : BuildType(3, "release")
        }
    }

    /**
     * Container for version codes used in gradle dependencies.
     * @see dependencies.Dependencies
     */
    object Dependencies {
        const val play_services_coroutines = "1.6.0"
        const val kotlin = "1.6.0"
        const val material = "1.5.0"
        const val hilt = "1.0.0"
        const val hilt_android = "2.40.5"
        const val timber = "5.0.1"
        const val gradle = "4.1.3"
        const val oss_app = "17.0.0"
        const val oss_root = "0.10.4"
        const val crashlytics = "2.4.1"
        const val google_services = "4.3.5"
        const val play_services_auth = "19.0.0"
        const val firebase_bom = "26.5.0"

        /**
         * AndroidX dependency codes
         * @see dependencies.Dependencies.Android
         */
        object Androidx {
            const val splashscreen = "1.0.0-beta01"
            const val app_compat = "1.4.1"
            const val constraint_layout = "2.1.3"
            const val recycler = "1.2.1"
            const val multidex = "2.0.1"
            const val ktx_core = "1.7.0"
            const val google_shortcuts = "1.0.0"
            const val lifecycle = "2.4.0"
            const val room = "2.4.1"
            const val navigation = "2.4.0"
            const val paging = "3.1.0"
            const val datastore = "1.0.0"
            const val activity = "1.4.0"
            const val fragment = "1.4.1"
            const val workmanager = "2.7.1"
        }
    }
}
