package com.saksham.deskcompanion.screens
import com.saksham.deskcompanion.design.DeskDesign
import android.view.View
import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.saksham.deskcompanion.DeskAgent
import com.saksham.deskcompanion.DeskApi
import android.app.AlertDialog
import android.widget.ScrollView
import java.util.concurrent.Executors

class AgentsScreen(
    context: Context,
    private val api: DeskApi
) : LinearLayout(context) {

    private val executor =
        Executors.newSingleThreadExecutor()

    private val container =
        LinearLayout(context)
	private val colors =
		DeskDesign.palette(context)
    init {

        orientation = VERTICAL
        setPadding(32)

        addView(
            TextView(context).apply {
                text = "Agents"
                textSize = 28f
                setTextColor(colors.foreground)
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
                    setTextColor(colors.secondary)
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

                    DeskDesign.applyOutlinedSurface(this)
                }

            card.addView(
                TextView(context).apply {
                    text = agent.name
                    textSize = 20f
                    setTextColor(colors.foreground)
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

            card.setOnClickListener {
                openAgent(agent)
            }

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
                setTextColor(colors.foreground)
            }
        )
    }

    private fun openAgent(
        agent: DeskAgent
    ) {

        val loadingDialog =
            AlertDialog.Builder(context)
                .setTitle(agent.name)
                .setMessage("Loading agent...")
                .setCancelable(false)
                .create()

        loadingDialog.show()

        executor.execute {

            try {

                val details =
                    api.getAgent(agent.name)

                post {

                    loadingDialog.dismiss()

                    val prompt =
                        details.systemPrompt
                            ?.takeIf { it.isNotBlank() }
                            ?: "No system prompt found."

                    val textView =
                        TextView(context).apply {

                            text = prompt

                            textSize = 14f

                            setTextColor(colors.secondary)

                            setPadding(
                                24,
                                24,
                                24,
                                24
                            )

                            setTextIsSelectable(true)
                        }

                    val scrollView =
                        ScrollView(context).apply {
                            addView(textView)
                        }

                    AlertDialog.Builder(context)
                        .setTitle(
                            "${agent.name} — System Prompt"
                        )
                        .setView(scrollView)
                        .setPositiveButton(
                            "Close",
                            null
                        )
                        .show()
                }

            } catch (exception: Exception) {

                post {

                    loadingDialog.dismiss()

                    AlertDialog.Builder(context)
                        .setTitle("Could not open agent")
                        .setMessage(
                            exception.message
                                ?: "Unknown error"
                        )
                        .setPositiveButton(
                            "OK",
                            null
                        )
                        .show()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        executor.shutdownNow()
        super.onDetachedFromWindow()
    }
}
