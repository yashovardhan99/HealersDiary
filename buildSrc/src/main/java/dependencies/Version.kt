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
}