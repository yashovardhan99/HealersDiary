package com.yashovardhan99.healersdiary.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

data class Header(val icon: Icon, val title: String, val optionIcon: Icon?)
data class Icon(val drawable: Drawable?, val description: String?, val clickable: Boolean)

fun Context.getIcon(@DrawableRes drawable: Int, description: String? = null, clickable: Boolean = false) =
        Icon(ResourcesCompat.getDrawable(resources, drawable, theme), description, clickable)