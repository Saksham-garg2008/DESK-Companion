package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.saksham.deskcompanion.DeskAgent
import com.saksham.deskcompanion.DeskApi
import java.util.concurrent.Executors

class AgentsScreen(
    context: Context,
    private val api: DeskApi
) : LinearLayout(context) {

    private val executor =
        Executors.newSingleThreadExecutor()

    private val container =
        LinearLayout(context)

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
                text = "Agents currently configured in DESK."
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(0, 0, 0, 24)
            }
        )

        container.orientation = VERTICAL

        addView(container)

        loadAgents()
    }

    private fun loadAgents() {

        executor.execute {

            try {

                val agents =
                    api.getAgents()

                post {
                    displayAgents(agents)
                }

            } catch (exception: Exception) {

                post {
                    showError(
                        exception.message
                            ?: "Could not load agents."
                    )
                }
            }
        }
    }

    private fun displayAgents(
        agents: List<DeskAgent>
    ) {

        container.removeAllViews()

        if (agents.isEmpty()) {

            container.addView(
                TextView(context).apply {
                    text = "No agents found."
                    textSize = 15f
                    setTextColor(Color.GRAY)
                }
            )

            return
        }

        for (agent in agents) {

            val card =
                LinearLayout(context).apply {

                    orientation = VERTICAL
                    setPadding(
                        20,
                        20,
                        20,
                        20
                    )

                    setBackgroundColor(
                        Color.rgb(
                            245,
                            245,
                            245
                        )
                    )
                }

            card.addView(
                TextView(context).apply {
                    text = agent.name
                    textSize = 20f
                    setTextColor(Color.BLACK)
                }
            )

            card.addView(
                TextView(context).apply {
                    text =
                        "${agent.backend} • ${agent.model}"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                    setPadding(
                        0,
                        8,
                        0,
                        0
                    )
                }
            )

            container.addView(
                card,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(
                        0,
                        0,
                        0,
                        16
                    )
                }
            )
        }
    }

    private fun showError(message: String) {

        container.removeAllViews()

        container.addView(
            TextView(context).apply {
                text = message
                textSize = 15f
                setTextColor(Color.RED)
            }
        )
    }

    override fun onDetachedFromWindow() {
        executor.shutdownNow()
        super.onDetachedFromWindow()
    }
}
