
package com.saksham.deskcompanion.screens
import com.saksham.deskcompanion.design.DeskDesign
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setPadding
import com.saksham.deskcompanion.AgentArtifacts
import com.saksham.deskcompanion.DeskApi
import java.util.concurrent.Executors

class WorkspaceScreen(
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
                text = "Artifacts"
                textSize = 28f
                setTextColor(colors.foreground)
                setPadding(0, 0, 0, 8)
            }
        )

        addView(
            TextView(context).apply {
                text = "Files created by your DESK agents"
                textSize = 16f
                setTextColor(colors.secondary)
                setPadding(0, 0, 0, 24)
            }
        )

        container.orientation = VERTICAL

        addView(
            ScrollView(context).apply {
                addView(container)
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
        )

        loadArtifacts()
    }

    private fun loadArtifacts() {
        executor.execute {
            try {
                val artifacts = api.getAllArtifacts()

                post {
                    displayArtifacts(artifacts)
                }
            } catch (exception: Exception) {
                post {
                    showError(
                        exception.message
                            ?: "Could not load artifacts."
                    )
                }
            }
        }
    }

    private fun displayArtifacts(
        groups: List<AgentArtifacts>
    ) {
        container.removeAllViews()

        val nonEmptyGroups = groups.filter {
            it.artifacts.length() > 0
        }

        if (nonEmptyGroups.isEmpty()) {
            container.addView(
                TextView(context).apply {
                    text = "No artifacts have been created yet."
                    textSize = 15f
                    setTextColor(colors.secondary)
                    setPadding(8, 16, 8, 16)
                }
            )
            return
        }

        for (group in nonEmptyGroups) {
            container.addView(
                TextView(context).apply {
                    text = group.agent
                    textSize = 20f
                    setTextColor(colors.foreground)
                    setPadding(0, 16, 0, 12)
                }
            )

            for (i in 0 until group.artifacts.length()) {
                val artifact =
                    group.artifacts.getJSONObject(i)

                val filename =
                    artifact.optString("filename", "Untitled")

                val type =
                    artifact.optString("type", "file")

                val language =
                    artifact.optString("language", "")

                val currentVersion =
                    artifact.optInt("current_version", 1)

                val versionCount =
                    artifact.optInt("version_count", 1)

                val details = buildString {
                    append(type)

                    if (language.isNotBlank()) {
                        append(" · ")
                        append(language)
                    }

                    append("\nVersion ")
                    append(currentVersion)
                    append(" of ")
                    append(versionCount)
                }

                val card = LinearLayout(context).apply {
                    orientation = VERTICAL
                    setPadding(20)

                    DeskDesign.applyOutlinedSurface(this)

                    addView(
                        TextView(context).apply {
                            text = filename
                            textSize = 16f
                            setTextColor(colors.foreground)
                        }
                    )

                    addView(
                        TextView(context).apply {
                            text = details
                            textSize = 14f
                            setTextColor(colors.secondary)
                            setLineSpacing(2f, 1.0f)
							setPadding(0, 8, 0, 0)
                        }
                    )

                    isClickable = true
                    isFocusable = true

                    setOnClickListener {
                        showArtifactDetails(
                            group.agent,
                            filename,
                            details
                        )
                    }
                }

                container.addView(
                    card,
                    LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 12)
                    }
                )
            }
        }
    }

    private fun showArtifactDetails(
        agentName: String,
        filename: String,
        details: String
    ) {
        AlertDialog.Builder(context)
            .setTitle(filename)
            .setMessage(
                "Created by: $agentName\n\n$details\n\n" +
                    "Opening artifact contents will be added " +
                    "after the DESK content endpoint is implemented."
            )
            .setPositiveButton("Close", null)
            .show()
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

    override fun onDetachedFromWindow() {
        executor.shutdownNow()
        super.onDetachedFromWindow()
    }
}
