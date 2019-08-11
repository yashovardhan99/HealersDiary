package com.yashovardhan99.healersdiary.helpers

import android.os.Build
import android.text.Html
import android.text.Spanned

/**
 * Created by Yashovardhan99 on 02-07-2018 as a part of HealersDiary.
 */

object HtmlCompat {

    fun fromHtml(source: String): Spanned {
        @Suppress("DEPRECATION")
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT)
            else -> Html.fromHtml(source)
        }
    }
}
