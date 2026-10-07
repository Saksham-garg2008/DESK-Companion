package com.saksham.deskcompanion.screens

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setPadding
import com.saksham.deskcompanion.DeskAgent
import com.saksham.deskcompanion.DeskApi
import com.saksham.deskcompanion.ChatResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatScreen(
    context: Context,
    private val deskApi: DeskApi
) : LinearLayout(context) {

    private val agentSpinner: Spinner
    private val conversation: LinearLayout
    private val messageInput: EditText
    private val sendButton: Button
    private val imageButton: Button

    private var agents: List<DeskAgent> = emptyList()

    init {
        orientation = VERTICAL
        setPadding(24)

        // ─────────────────────────────────────────────
        // Agent selector
        // ─────────────────────────────────────────────

        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(context).apply {
            text = "Chat"
            textSize = 24f
            setTextColor(Color.BLACK)
        }

        header.addView(
            title,
            LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        agentSpinner = Spinner(context)

        header.addView(
            agentSpinner,
            LayoutParams(
                180,
                LayoutParams.WRAP_CONTENT
            )
        )

        addView(header)

        // ─────────────────────────────────────────────
        // Conversation area
        // ─────────────────────────────────────────────

        val scrollView = ScrollView(context).apply {
            isFillViewport = true
        }

        conversation = LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(8, 24, 8, 24)
        }

        scrollView.addView(conversation)

        addView(
            scrollView,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // ─────────────────────────────────────────────
        // Bottom input area
        // ─────────────────────────────────────────────

        val inputRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        imageButton = Button(context).apply {
            text = "+"
            isEnabled = false
        }

        inputRow.addView(
            imageButton,
            LayoutParams(
                52,
                LayoutParams.WRAP_CONTENT
            )
        )

        messageInput = EditText(context).apply {
            hint = "Message..."
            setSingleLine(false)
            maxLines = 4
        }

        inputRow.addView(
            messageInput,
            LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        sendButton = Button(context).apply {
            text = "➤"
        }

        inputRow.addView(
            sendButton,
            LayoutParams(
                60,
                LayoutParams.WRAP_CONTENT
            )
        )

        addView(inputRow)

        // ─────────────────────────────────────────────
        // Events
        // ─────────────────────────────────────────────

        sendButton.setOnClickListener {
            sendMessage()
        }

        loadAgents()
    }

    private fun loadAgents() {

        CoroutineScope(Dispatchers.Main).launch {

            try {

                val loadedAgents = withContext(Dispatchers.IO) {
                    deskApi.getAgents()
                }

                agents = loadedAgents

                val names = agents.map { it.name }

                agentSpinner.adapter =
                    ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_dropdown_item,
                        names
                    )

                messageInput.isEnabled = agents.isNotEmpty()
                sendButton.isEnabled = agents.isNotEmpty()

                if (agents.isNotEmpty()) {
                    conversation.removeAllViews()

                    addSystemMessage(
                        "Chat with ${agents[0].name}"
                    )
                } else {
                    addSystemMessage(
                        "No agents available."
                    )
                }

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Failed to load agents: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun sendMessage() {

        val message =
            messageInput.text.toString().trim()

        if (message.isEmpty()) {
            return
        }

        if (agents.isEmpty()) {
            return
        }

        val selectedAgent =
            agents[agentSpinner.selectedItemPosition]

        addUserMessage(message)

        messageInput.text.clear()

        sendButton.isEnabled = false
        messageInput.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {

            try {

                val result: ChatResponse =
                    withContext(Dispatchers.IO) {

                        deskApi.sendChat(
                            agentName = selectedAgent.name,
                            message = message
                        )
                    }

                addAgentMessage(
                    selectedAgent.name,
                    result.response
                )

            } catch (e: Exception) {

                addSystemMessage(
                    "Error: ${e.message ?: "Unable to contact DESK."}"
                )

            } finally {

                sendButton.isEnabled = true
                messageInput.isEnabled = true
                messageInput.requestFocus()
            }
        }
    }

    // ─────────────────────────────────────────────
    // Conversation messages
    // ─────────────────────────────────────────────

    private fun addUserMessage(message: String) {

        addMessage(
            sender = "You",
            message = message
        )
    }

    private fun addAgentMessage(
        agentName: String,
        message: String
    ) {

        addMessage(
            sender = agentName,
            message = message
        )
    }

    private fun addSystemMessage(message: String) {

        addMessage(
            sender = "DESK",
            message = message
        )
    }

    private fun addMessage(
        sender: String,
        message: String
    ) {

        val container =
            LinearLayout(context).apply {
                orientation = VERTICAL
                setPadding(8, 8, 8, 16)
            }

        val senderView =
            TextView(context).apply {
                text = sender
                textSize = 13f
                setTextColor(Color.GRAY)
            }

        val messageView =
            TextView(context).apply {
                text = message
                textSize = 16f
                setTextColor(Color.BLACK)
                setPadding(0, 4, 0, 0)
            }

        container.addView(senderView)
        container.addView(messageView)

        conversation.addView(container)

        conversation.post {
            conversation.parent?.let {
                if (it is ScrollView) {
                    it.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }
}
