package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding

class AgentsScreen(
    context: Context
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setPadding(32)

        addView(
            TextView(context).apply {
                text = "Agents"
                textSize = 28f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 24)
            }
        )

        addView(
            TextView(context).apply {
                text = "Your DESK agents will appear here."
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(0, 0, 0, 24)
            }
        )

        addView(
            TextView(context).apply {
                text = "No agent data loaded yet."
                textSize = 15f
                setTextColor(Color.GRAY)
                setPadding(0, 0, 0, 32)
            }
        )

        addView(
            Button(context).apply {
                text = "+ Create Agent"
                isEnabled = false
            }
        )

        addView(
            Button(context).apply {
                text = "Fire Agent"
                isEnabled = false
            }
        )
    }
}
