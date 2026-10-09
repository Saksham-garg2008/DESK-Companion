
package com.saksham.deskcompanion.design

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

object DeskShell {

    fun createHeader(
        context: Context,
        onIndexClick: () -> Unit
    ): LinearLayout {
        val colors = DeskDesign.palette(context)

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                DeskDesign.dp(context, DeskDesign.Space.LG),
                DeskDesign.dp(context, DeskDesign.Space.SM),
                DeskDesign.dp(context, DeskDesign.Space.LG),
                DeskDesign.dp(context, DeskDesign.Space.SM)
            )
            setBackgroundColor(colors.background)

            val wordmark = TextView(context).apply {
                text = "DESK"
                textSize = 23f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(colors.foreground)
                gravity = Gravity.CENTER_VERTICAL
            }

            addView(
                wordmark,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val indexButton = TextView(context).apply {
                text = "☰"
                textSize = 24f
                gravity = Gravity.CENTER
                contentDescription = "Open Index"
                setTextColor(colors.foreground)
                isClickable = true
                isFocusable = true
                setPadding(
                    DeskDesign.dp(context, 12),
                    DeskDesign.dp(context, 8),
                    DeskDesign.dp(context, 12),
                    DeskDesign.dp(context, 8)
                )
                setOnClickListener { onIndexClick() }
            }

            addView(
                indexButton,
                LinearLayout.LayoutParams(
                    DeskDesign.dp(context, 48),
                    DeskDesign.dp(context, 48)
                )
            )
        }
    }

    fun createIndex(
        context: Context,
        onAgents: () -> Unit,
        onMemory: () -> Unit,
        onArtifacts: () -> Unit,
        onSettings: () -> Unit
    ): View {
        val colors = DeskDesign.palette(context)

        val screen = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(colors.background)
            DeskDesign.applyScreenPadding(
                this,
                horizontalDp = DeskDesign.Space.LG,
                verticalDp = DeskDesign.Space.LG
            )
        }

        val heading = TextView(context).apply {
            text = "Index"
            DeskDesign.styleDisplay(this)
            setPadding(
                0,
                DeskDesign.dp(context, DeskDesign.Space.LG),
                0,
                DeskDesign.dp(context, DeskDesign.Space.LG)
            )
        }
        screen.addView(heading)

        val destinations = listOf(
            Triple("01", "Agents", onAgents),
            Triple("02", "Memory", onMemory),
            Triple("03", "Artifacts", onArtifacts),
            Triple("04", "Settings", onSettings)
        )

        destinations.forEach { (number, label, action) ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                isClickable = true
                isFocusable = true
                setPadding(
                    0,
                    DeskDesign.dp(context, 20),
                    0,
                    DeskDesign.dp(context, 20)
                )
                setOnClickListener { action() }
            }

            val numberView = TextView(context).apply {
                text = number
                textSize = DeskDesign.Type.SECONDARY
                setTextColor(colors.secondary)
            }

            row.addView(
                numberView,
                LinearLayout.LayoutParams(
                    DeskDesign.dp(context, 48),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val labelView = TextView(context).apply {
                text = label
                DeskDesign.styleSectionTitle(this)
            }

            row.addView(
                labelView,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val arrow = TextView(context).apply {
                text = "↗"
                textSize = 20f
                setTextColor(colors.secondary)
            }
            row.addView(arrow)

            screen.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val divider = View(context).apply {
                DeskDesign.applyDivider(this)
            }

            screen.addView(
                divider,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    DeskDesign.dp(context, 1)
                )
            )
        }

        return screen
    }
}
