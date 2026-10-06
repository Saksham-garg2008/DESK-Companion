package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.saksham.deskcompanion.DeskApi
import java.util.concurrent.Executors

class MemoryScreen(
    context: Context,
    private val api: DeskApi
) : LinearLayout(context) {

    private val executor =
        Executors.newSingleThreadExecutor()

    private val content =
        TextView(context)

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
                text = "DESK Agent Memory"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 16)
            }
        )

        content.apply {
            text = "Loading..."
            textSize = 15f
            setTextColor(Color.DKGRAY)
        }

        addView(content)

        loadMemory()
    }

    private fun loadMemory() {

        executor.execute {

            try {

                val agents =
                    api.getAgents()

                if (agents.isEmpty()) {

                    post {
                        content.text =
                            "No agents available."
                    }

                    return@execute
                }

                val agent =
                    agents.first()

                val memory =
                    api.getMemory(agent.name)

                post {

                    content.text =
                        if (memory.isBlank()) {
                            "${agent.name}\n\nMemory is empty."
                        } else {
                            "${agent.name}\n\n$memory"
                        }
                }

            } catch (exception: Exception) {

                post {

                    content.text =
                        exception.message
                            ?: "Could not load memory."

                    content.setTextColor(
                        Color.RED
                    )
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        executor.shutdownNow()
        super.onDetachedFromWindow()
    }
}
