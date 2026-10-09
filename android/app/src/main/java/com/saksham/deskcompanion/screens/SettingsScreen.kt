package com.saksham.deskcompanion.screens
import com.saksham.deskcompanion.design.DeskDesign
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

private val colors =
    DeskDesign.palette(context)

    init {
        orientation = VERTICAL
        setPadding(32)

        addView(
            TextView(context).apply {
                text = "Settings"
                textSize = 28f
                setTextColor(colors.foreground)
                setPadding(0, 0, 0, 32)
            }
        )

        addView(
            TextView(context).apply {
                text = "Connection"
                textSize = 18f
                setTextColor(colors.foreground)
                setPadding(0, 0, 0, 12)
            }
        )

        addView(
            TextView(context).apply {
				text = "DESK\n$host:$port"
				textSize = 16f
				setTextColor(colors.secondary)
				setLineSpacing(3f, 1.0f)
				setPadding(0, 0, 0, 24)
			}
        )

        addView(
            TextView(context).apply {
                text = "Device is paired with this DESK installation."
				textSize = 15f
				setTextColor(colors.secondary)
				setLineSpacing(3f, 1.0f)
				setPadding(0, 0, 0, 24)
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
