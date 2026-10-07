package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setPadding
import com.saksham.deskcompanion.DeskAgent
import com.saksham.deskcompanion.DeskApi
import java.util.concurrent.Executors

class MemoryScreen(
    context: Context,
    private val api: DeskApi
) : LinearLayout(context) {

    private val executor =
        Executors.newSingleThreadExecutor()

    private val agentContainer =
        LinearLayout(context)

    private val memoryContainer =
        LinearLayout(context)

    private val memoryTitle =
        TextView(context)

    private val memoryContent =
        TextView(context)

    init {

        orientation = VERTICAL
        setPadding(32)

        // Page title
        addView(
            TextView(context).apply {
                text = "Memory"
                textSize = 28f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 24)
            }
        )

        // Description
        addView(
            TextView(context).apply {
                text = "Select an agent to view its DESK memory."
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(0, 0, 0, 20)
            }
        )

        // Agent selector
        agentContainer.orientation = VERTICAL

        addView(
            agentContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // Memory view
        memoryContainer.orientation = VERTICAL
        memoryContainer.visibility = View.GONE

        // Back button
        val backButton =
            TextView(context).apply {
                text = "← All agents"
                textSize = 16f
                setTextColor(Color.BLACK)
                setPadding(0, 12, 0, 20)

                isClickable = true
                isFocusable = true

                setOnClickListener {
                    showAgentList()
                }
            }

        memoryContainer.addView(backButton)

        // Selected agent name
        memoryTitle.apply {
            textSize = 22f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 12)
        }

        memoryContainer.addView(memoryTitle)

        // Memory content
        memoryContent.apply {
            textSize = 15f
            setTextColor(Color.DKGRAY)
        }

        val memoryScroll =
            ScrollView(context).apply {

                addView(
                    memoryContent,
                    ScrollView.LayoutParams(
                        ScrollView.LayoutParams.MATCH_PARENT,
                        ScrollView.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        memoryContainer.addView(

        addView(
            memoryContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
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

        agentContainer.removeAllViews()

        agentContainer.visibility = View.VISIBLE
        memoryContainer.visibility = View.GONE

        if (agents.isEmpty()) {

            agentContainer.addView(
                TextView(context).apply {
                    text = "No agents available."
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
                        Color.rgb(245, 245, 245)
                    )

                    isClickable = true
                    isFocusable = true

                    setOnClickListener {
                        loadMemory(agent)
                    }
                }

            // Agent name
            card.addView(
                TextView(context).apply {
                    text = agent.name
                    textSize = 20f
                    setTextColor(Color.BLACK)
                }
            )

            // Action description
            card.addView(
                TextView(context).apply {
                    text = "View memory"
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

            agentContainer.addView(
                card,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(
                        0,
                        0,
                        0,
                        12
                    )
                }
            )
        }
    }

    private fun loadMemory(
        agent: DeskAgent
    ) {

        // Switch from agent list to memory view
        agentContainer.visibility = View.GONE
        memoryContainer.visibility = View.VISIBLE

        memoryTitle.text = agent.name

        memoryContent.apply {
            text = "Loading memory..."
            setTextColor(Color.DKGRAY)
        }

        executor.execute {

            try {

                val memory =
                    api.getMemory(agent.name)

                post {

                    memoryContent.apply {

                        text =
                            if (memory.isBlank()) {
                                "Memory is empty."
                            } else {
                                memory
                            }

                        setTextColor(Color.DKGRAY)
                    }
                }

            } catch (exception: Exception) {

                post {

                    memoryContent.apply {

                        text =
                            exception.message
                                ?: "Could not load memory."

                        setTextColor(Color.RED)
                    }
                }
            }
        }
    }

    private fun showAgentList() {

        memoryContainer.visibility = View.GONE
        agentContainer.visibility = View.VISIBLE
    }

    private fun showError(
        message: String
    ) {

        agentContainer.removeAllViews()

        agentContainer.visibility = View.VISIBLE
        memoryContainer.visibility = View.GONE

        agentContainer.addView(
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
