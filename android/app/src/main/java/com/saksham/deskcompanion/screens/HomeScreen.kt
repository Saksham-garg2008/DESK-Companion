package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding

class HomeScreen(
    context: Context,
    private val host: String,
    private val port: Int
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setPadding(32)

        addView(
            TextView(context).apply {
                text = "Home"
                textSize = 28f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 24)
            }
        )

        addView(
            TextView(context).apply {
                text = "● Connected"
                textSize = 18f
                setTextColor(Color.rgb(40, 140, 80))
                setPadding(0, 0, 0, 16)
            }
        )

        addView(
            TextView(context).apply {
                text = "DESK\n$host:$port"
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(0, 0, 0, 32)
            }
        )

        addView(
            TextView(context).apply {
                text = "Agents"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 8)
            }
        )

        addView(
            TextView(context).apply {
                text = "—"
                textSize = 32f
                setTextColor(Color.BLACK)
            }
        )

        addView(
            TextView(context).apply {
                text = "Agent count will appear here once DESK data is connected."
                textSize = 14f
                setTextColor(Color.GRAY)
                setPadding(0, 4, 0, 0)
            }
        )
    }
}
