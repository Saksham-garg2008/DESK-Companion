package com.saksham.deskcompanion.screens

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setPadding
import com.saksham.deskcompanion.DeskApi
import com.saksham.deskcompanion.WorkspaceItem
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
                    displayWorkspace(items)
                }

            } catch (exception: Exception) {

                post {
                    showError(
                        exception.message
                            ?: "Could not load workspace."
                    )
                }
            }
        }
    }

    private fun displayWorkspace(
        items: List<WorkspaceItem>
    ) {

        container.removeAllViews()

        if (items.isEmpty()) {

            container.addView(
                TextView(context).apply {
                    text = "Workspace is empty."
                    textSize = 15f
                    setTextColor(Color.GRAY)
                }
            )

            return
        }

        for (item in items) {

            val fileView =
                TextView(context).apply {

                    text =
                        "${item.path}\n" +
                        "${item.size} bytes"

                    textSize = 15f
                    setTextColor(Color.DKGRAY)

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

                    isClickable = true

                    setOnClickListener {
                        openFile(item)
                    }
                }

            container.addView(
                fileView,
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

    private fun openFile(
        item: WorkspaceItem
    ) {

        val loadingDialog =
            AlertDialog.Builder(context)
                .setTitle(item.name)
                .setMessage("Loading file...")
                .setCancelable(false)
                .create()

        loadingDialog.show()

        executor.execute {

            try {

                val content =
                    api.getWorkspaceFile(
                        item.path
                    )

                post {

                    loadingDialog.dismiss()

                    showFileContent(
                        item.name,
                        content
                    )
                }

            } catch (exception: Exception) {

                post {

                    loadingDialog.dismiss()

                    showErrorDialog(
                        exception.message
                            ?: "Could not open file."
                    )
                }
            }
        }
    }

    private fun showFileContent(
        name: String,
        content: String
    ) {

        val textView =
            TextView(context).apply {

                text = content

                textSize = 14f

                setTextColor(
                    Color.DKGRAY
                )

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
            .setTitle(name)
            .setView(scrollView)
            .setPositiveButton(
                "Close",
                null
            )
            .show()
    }

    private fun showError(
        message: String
    ) {

        container.removeAllViews()

        container.addView(
            TextView(context).apply {
                text = message
                textSize = 15f
                setTextColor(Color.RED)
            }
        )
    }

    private fun showErrorDialog(
        message: String
    ) {

        AlertDialog.Builder(context)
            .setTitle("Could not open file")
            .setMessage(message)
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    override fun onDetachedFromWindow() {

        executor.shutdownNow()

        super.onDetachedFromWindow()
    }
}
