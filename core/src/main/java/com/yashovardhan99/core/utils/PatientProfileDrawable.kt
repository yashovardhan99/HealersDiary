package com.yashovardhan99.core.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import timber.log.Timber
import kotlin.math.min
import kotlin.random.Random

class PatientProfileDrawable(name: String) : Drawable() {
    @Suppress("SpellCheckingInspection")
    private val backgrounds = listOf(
        "#FFC0CB",
        "#FFE4E1",
        "#D3FFCE",
        "#B0E0E6",
        "#C6E2FF",
        "#FAEBD7",
        "#F0F8FF",
        "#FFB6C1",
        "#FA8072",
        "#FFF68F",
        "#FFC3A0",
        "#FFDAB9",
        "#B4EEB4",
        "#B6FCD5",
        "#DAA520",
        "#F5F5DC"
    )
    private val random = Random(name.hashCode())
    private val background = Color.parseColor(backgrounds.random(random))

    private val letter = name.first().toUpperCase().toString()
    override fun draw(canvas: Canvas) {
        Timber.d("Canvas $canvas for letter $letter")
        Timber.d("Background color = $background")
        canvas.drawColor(background)
        val minSize = min(bounds.width(), bounds.height())
        Timber.d("Min size = $minSize")
        val margin = minSize * .05f // 5% margin
        val textPaint = TextPaint().apply {
            typeface = Typeface.SANS_SERIF
            textAlign = Paint.Align.CENTER
            textSize = minSize / 2f - margin
        }
        val textBounds = Rect()
        textPaint.getTextBounds(letter, 0, 1, textBounds)
        val height = textBounds.height()
        val bottomY = bounds.height() / 2f + height / 2f
        canvas.drawText(letter, bounds.width() / 2f, bottomY, textPaint)
    }

    override fun setAlpha(alpha: Int) {
        //
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        //
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

}