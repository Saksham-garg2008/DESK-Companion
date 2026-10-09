package com.saksham.deskcompanion.design

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView

/**
 * Central visual authority for DESK Companion.
 *
 * Keep visual tokens and reusable styling here. Screen classes should own
 * data loading and actions, but should not invent their own palette, spacing,
 * typography, or surface treatments.
 *
 * This first layer intentionally introduces no new dependencies and does not
 * change screen behavior by itself. Existing screens can adopt these helpers
 * incrementally without moving API or connection logic into this file.
 */
object DeskDesign {

    data class Palette(
        val background: Int,
        val surface: Int,
        val foreground: Int,
        val secondary: Int,
        val divider: Int,
        val outline: Int
    )

    /** Spacing tokens are expressed in dp and converted at the point of use. */
    object Space {
        const val XS = 4
        const val SM = 8
        const val MD = 16
        const val LG = 24
        const val XL = 32
        const val XXL = 40
    }

    /** Typography sizes are expressed in sp. */
    object Type {
        const val DISPLAY = 38f
        const val PAGE_TITLE = 34f
        const val SECTION_TITLE = 22f
        const val BODY = 16f
        const val SECONDARY = 14f
        const val CAPTION = 12f
    }

    fun palette(context: Context): Palette {
        val nightMode = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

        return if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            Palette(
                background = Color.rgb(12, 12, 12),
                surface = Color.rgb(20, 20, 20),
                foreground = Color.rgb(245, 245, 245),
                secondary = Color.rgb(170, 170, 170),
                divider = Color.rgb(48, 48, 48),
                outline = Color.rgb(58, 58, 58)
            )
        } else {
            Palette(
                background = Color.rgb(250, 250, 250),
                surface = Color.WHITE,
                foreground = Color.rgb(17, 17, 17),
                secondary = Color.rgb(96, 96, 96),
                divider = Color.rgb(224, 224, 224),
                outline = Color.rgb(216, 216, 216)
            )
        }
    }

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun styleScreen(view: View) {
        view.setBackgroundColor(palette(view.context).background)
    }

    fun applyScreenPadding(
        view: View,
        horizontalDp: Int = Space.LG,
        verticalDp: Int = Space.LG
    ) {
        val horizontal = dp(view.context, horizontalDp)
        val vertical = dp(view.context, verticalDp)
        view.setPadding(horizontal, vertical, horizontal, vertical)
    }

    fun styleDisplay(textView: TextView) {
        textView.textSize = Type.DISPLAY
        textView.setTextColor(palette(textView.context).foreground)
    }

    fun stylePageTitle(textView: TextView) {
        textView.textSize = Type.PAGE_TITLE
        textView.setTextColor(palette(textView.context).foreground)
    }

    fun styleSectionTitle(textView: TextView) {
        textView.textSize = Type.SECTION_TITLE
        textView.setTextColor(palette(textView.context).foreground)
    }

    fun styleBody(textView: TextView) {
        textView.textSize = Type.BODY
        textView.setTextColor(palette(textView.context).foreground)
    }

    fun styleSecondary(textView: TextView) {
        textView.textSize = Type.SECONDARY
        textView.setTextColor(palette(textView.context).secondary)
    }

    fun styleCaption(textView: TextView) {
        textView.textSize = Type.CAPTION
        textView.setTextColor(palette(textView.context).secondary)
    }

    fun applyOutlinedSurface(view: View, cornerRadiusDp: Int = 14) {
        val colors = palette(view.context)
        view.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(view.context, cornerRadiusDp).toFloat()
            setColor(colors.surface)
            setStroke(dp(view.context, 1), colors.outline)
        }
    }

    fun applyDivider(view: View) {
        view.setBackgroundColor(palette(view.context).divider)
    }
}
