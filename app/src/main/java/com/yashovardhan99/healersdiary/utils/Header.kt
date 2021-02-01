package com.yashovardhan99.healersdiary.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.yashovardhan99.healersdiary.R

data class Header(val icon: Icons, val title: String, val optionIcon: Icons?)

fun buildHeader(icon: Icons, title: String, optionIcon: Icons? = null): Header {
    return Header(icon, title, optionIcon)
}

fun Context.buildHeader(icon: Icons, @StringRes title: Int, optionIcon: Icons? = null) = buildHeader(icon, getString(title), optionIcon)

fun Context.buildHeader(@DrawableRes icon: Int, @StringRes title: Int, optionIcon: Icons? = null) = buildHeader(Icons.CustomStatic(icon), getString(title), optionIcon)

sealed class Icons(@DrawableRes val drawable: Int, @StringRes val description: Int?, val clickable: Boolean) {
    object Add : Icons(R.drawable.add, R.string.add_new_record, true)
    object Save : Icons(R.drawable.save, R.string.save, true)
    object Back : Icons(R.drawable.back, R.string.back, true)
    object Settings : Icons(R.drawable.settings, R.string.settings, true)
    object Close : Icons(R.drawable.cross, R.string.close, true)
    object Delete : Icons(R.drawable.ic_baseline_delete_forever_24, R.string.delete, true)
    class CustomButton(@DrawableRes drawable: Int, @StringRes description: Int) : Icons(drawable, description, true)
    class CustomStatic(@DrawableRes drawable: Int, @StringRes description: Int? = null) : Icons(drawable, description, false)
}