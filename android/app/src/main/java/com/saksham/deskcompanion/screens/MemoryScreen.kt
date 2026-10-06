package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding

class MemoryScreen(
    context: Context
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setPadding(32)

        addView(
            TextView(context).apply {
                text = "Memory"
                textSize = 28f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 24)
            }
        )

        addView(
            TextView(context).apply {
                text = "DESK Memory"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 12)
            }
        )

        addView(
            TextView(context).apply {
                text = "Memory from DESK will appear here."
                textSize = 15f
                setTextColor(Color.GRAY)
            }
        )
    }
}
