package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding

class SettingsScreen(
    context: Context,
    private val host: String,
    private val port: Int,
    private val onDisconnect: () -> Unit
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setPadding(32)

        addView(
            TextView(context).apply {
                text = "Settings"
                textSize = 28f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 32)
            }
        )

        addView(
            TextView(context).apply {
                text = "Connection"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 12)
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
                text = "Device is paired with this DESK installation."
                textSize = 15f
                setTextColor(Color.GRAY)
                setPadding(0, 0, 0, 32)
            }
        )

        addView(
            Button(context).apply {
                text = "Disconnect / Re-pair"

                setOnClickListener {
                    onDisconnect()
                }
            }
        )
    }
}
