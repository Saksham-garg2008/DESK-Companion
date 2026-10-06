package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.saksham.deskcompanion.DeskApi
import java.util.concurrent.Executors

class HomeScreen(
    context: Context,
    private val host: String,
    private val port: Int,
    private val api: DeskApi
) : LinearLayout(context) {

    private val executor =
        Executors.newSingleThreadExecutor()

    private val agentCountText =
        TextView(context)

    private val statusText =
        TextView(context)

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

        statusText.apply {
            text = "● Connecting..."
            textSize = 18f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 16)
        }

        addView(statusText)

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

        agentCountText.apply {
            text = "..."
            textSize = 32f
            setTextColor(Color.BLACK)
        }

        addView(agentCountText)

        loadStatus()
    }

    private fun loadStatus() {

        executor.execute {

            try {

                val status =
                    api.getStatus()

                val count =
                    status.optInt(
                        "agent_count",
                        0
                    )

                post {

                    statusText.text =
                        "● Connected"

                    statusText.setTextColor(
                        Color.rgb(
                            40,
                            140,
                            80
                        )
                    )

                    agentCountText.text =
                        count.toString()
                }

            } catch (exception: Exception) {

                post {

                    statusText.text =
                        "● Connection error"

                    statusText.setTextColor(
                        Color.RED
                    )

                    agentCountText.text =
                        "—"
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        executor.shutdownNow()
        super.onDetachedFromWindow()
    }
}
