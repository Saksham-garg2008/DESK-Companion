package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
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

    init {

        orientation = VERTICAL
        setPadding(32)

        addView(
            TextView(context).apply {
                text = "Workspace"
                textSize = 28f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 24)
            }
        )

        addView(
            TextView(context).apply {
                text = "DESK Workspace"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 16)
            }
        )

        container.orientation = VERTICAL

        addView(container)

        loadWorkspace()
    }

    private fun loadWorkspace() {

        executor.execute {

            try {

                val items =
                    api.getWorkspace()

                post {

                    container.removeAllViews()

                    if (items.isEmpty()) {

                        container.addView(
                            TextView(context).apply {
                                text =
                                    "Workspace is empty."
                                textSize = 15f
                                setTextColor(
                                    Color.GRAY
                                )
                            }
                        )

                        return@post
                    }

                    for (item in items) {

                        container.addView(
                            TextView(context).apply {

                                text =
                                    "${item.path}\n" +
                                    "${item.size} bytes"

                                textSize = 15f
                                setTextColor(
                                    Color.DKGRAY
                                )

                                setPadding(
                                    0,
                                    0,
                                    0,
                                    16
                                )
                            }
                        )
                    }
                }

            } catch (exception: Exception) {

                post {

                    container.removeAllViews()

                    container.addView(
                        TextView(context).apply {
                            text =
                                exception.message
                                    ?: "Could not load workspace."
                            textSize = 15f
                            setTextColor(Color.RED)
                        }
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
