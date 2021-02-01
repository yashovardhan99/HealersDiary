package com.yashovardhan99.healersdiary.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.yashovardhan99.healersdiary.R

data class Header(val icon: Icon, val title: String, val optionIcon: Icon?)
data class Icon(val drawable: Drawable?, val description: String?, val clickable: Boolean)
sealed class Icons(@DrawableRes val drawable: Int, @StringRes val description: Int?, val clickable: Boolean) {
    object Add : Icons(R.drawable.add, R.string.add_new_record, true)
    object Save : Icons(R.drawable.save, R.string.save, true)
    object Back : Icons(R.drawable.back, R.string.back, true)
    object Settings : Icons(R.drawable.settings, R.string.settings, true)
    object Close : Icons(R.drawable.cross, R.string.close, true)
    class CustomButton(@DrawableRes drawable: Int, @StringRes description: Int) : Icons(drawable, description, true)
    class CustomStatic(@DrawableRes drawable: Int, @StringRes description: Int? = null) : Icons(drawable, description, false)
}

// TODO: 1/2/21 Switch all icons to using Icons class, reduce to single getIcons() and make others private
fun Context.getIcon(@DrawableRes drawable: Int, description: String? = null, clickable: Boolean = false) =
        Icon(ResourcesCompat.getDrawable(resources, drawable, theme), description, clickable)

fun Context.getIcon(icon: Icons, clickable: Boolean = icon.clickable): Icon {
    val description = icon.description?.let { getString(it) }
    return getIcon(icon.drawable, description, clickable)
}