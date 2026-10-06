package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.setPadding

class ChatScreen(
    context: Context
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setPadding(24)

        addView(
            TextView(context).apply {
                text = "Chat"
                textSize = 28f
                setTextColor(Color.BLACK)
                setPadding(8, 8, 8, 24)
            }
        )

        addView(
            TextView(context).apply {
                text = "Agent"
                textSize = 14f
                setTextColor(Color.GRAY)
                setPadding(8, 0, 8, 6)
            }
        )

        addView(
            Spinner(context).apply {
                isEnabled = false
            }
        )

        addView(
            TextView(context).apply {
                text = "Select an agent to start chatting."
                textSize = 15f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                setPadding(8, 48, 8, 48)
            }
        )

        addView(
            EditText(context).apply {
                hint = "Message DESK..."
                isEnabled = false
            }
        )

        val bottomRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
        }

        bottomRow.addView(
            Button(context).apply {
                text = "Image"
                isEnabled = false
            }
        )

        bottomRow.addView(
            Button(context).apply {
                text = "Send"
                isEnabled = false
            }
        )

        addView(bottomRow)
    }
}
