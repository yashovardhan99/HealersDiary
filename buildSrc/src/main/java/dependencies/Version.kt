package dependencies

import java.text.NumberFormat

object Version {

    object AppVersion {
        const val minSdk = 21
        const val targetSdk = 30
        const val compileSdk = 30
        private const val major = 2
        private const val minor = 0
        private const val patch = 0

        private val buildType: BuildType = BuildType.ALPHA
        private const val buildCode = 1

        private const val standard = (major * 100 + minor) * 100 + patch
        val versionCode = (standard * 10 + buildType.typeCode) * 100 + buildCode
        val versionName = buildVersionName()
        private fun buildVersionName(): String {
            val builder = StringBuilder("$major.$minor.$patch")
            return when {
                buildType is BuildType.RELEASE -> builder.toString()
                buildCode == 0 -> builder.append(builder.append("-${buildType.typeString}")).toString()
                else -> {
                    val code = NumberFormat.getInstance().apply { minimumIntegerDigits = 2 }.format(buildCode)
                    builder.append("-${buildType.typeString}").append(code).toString()
                }
            }
        }

        @Suppress("unused")
        private sealed class BuildType(val typeCode: Int, val typeString: String) {
            object ALPHA : BuildType(0, "alpha")
            object BETA : BuildType(1, "beta")
            object RC : BuildType(2, "rc")
            object RELEASE : BuildType(3, "release")
        }
    }

    object Dependencies {
        const val play_services_coroutines = "1.4.1"
        const val kotlin = "1.4.21"
        const val material = "1.2.1"
        const val hilt = "1.0.0-alpha03"
        const val hilt_android = "2.31.2-alpha"
        const val timber = "4.7.1"
        const val gradle = "4.1.2"
        const val oss_app = "17.0.0"
        const val oss_root = "0.10.2"
        const val crashlytics = "2.4.1"
        const val google_services = "4.3.5"
        const val play_services_auth = "19.0.0"
        const val firebase_bom = "26.4.0"

        object Androidx {
            const val app_compat = "1.2.0"
            const val constraint_layout = "2.0.4"
            const val recycler = "1.1.0"
            const val multidex = "2.0.1"
            const val ktx_core = "1.3.2"
            const val lifecycle = "2.2.0"
            const val room = "2.3.0-beta01"
            const val navigation = "2.3.0"
            const val paging = "3.0.0-alpha13"
            const val datastore = "1.0.0-alpha06"
            const val activity = "1.2.0-rc01"
            const val fragment = "1.3.0-rc02"
            const val workmanager = "2.5.0"
        }
    }
}