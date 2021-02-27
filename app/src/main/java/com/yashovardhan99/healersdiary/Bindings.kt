package com.yashovardhan99.healersdiary

import android.widget.TextView
import androidx.databinding.BindingAdapter
import timber.log.Timber

@BindingAdapter(value = ["showDebugVersion"])
fun TextView.setVersion(showDebugVersion: Boolean) {
    Timber.d("showDebug = $showDebugVersion")
    text = if (showDebugVersion && BuildConfig.DEBUG) {
        "Application id = ${BuildConfig.APPLICATION_ID}\n" +
                "Build type = ${BuildConfig.BUILD_TYPE}\n" +
                "Version code = ${BuildConfig.VERSION_CODE}\n" +
                "Version name = ${BuildConfig.VERSION_NAME}"
    } else {
        context.resources.getString(R.string.version_s, BuildConfig.VERSION_NAME)
    }
}