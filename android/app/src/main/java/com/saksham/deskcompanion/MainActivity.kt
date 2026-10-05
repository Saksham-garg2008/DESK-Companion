package com.saksham.deskcompanion

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48)
        }

        val title = TextView(this).apply {
            text = "DESK Companion"
            textSize = 28f
        }

        val subtitle = TextView(this).apply {
            text = "Connect to your DESK desktop application."
            textSize = 16f
        }

        layout.addView(title)
        layout.addView(subtitle)

        setContentView(layout)
    }
}
