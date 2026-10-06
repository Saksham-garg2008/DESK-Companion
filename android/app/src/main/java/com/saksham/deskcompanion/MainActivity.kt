package com.saksham.deskcompanion

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var codeInput: EditText
    private lateinit var statusText: TextView

    private lateinit var qrScanner: ActivityResultLauncher<ScanOptions>

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        qrScanner = registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                handleQrPayload(result.contents)
            }
        }

        if (hasSavedCredentials()) {
            buildConnectedScreen()
        } else {
            buildPairingScreen()
        }
    }

    // -------------------------------------------------------------------------
    // Pairing screen
    // -------------------------------------------------------------------------

    private fun buildPairingScreen() {

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

        val scanButton = Button(this).apply {
            text = "Scan QR Code"

            setOnClickListener {
                startQrScanner()
            }
        }

        val divider = TextView(this).apply {
            text = "\nOR\n"
            textSize = 16f
        }

        val codeLabel = TextView(this).apply {
            text = "Enter 6-digit code"
            textSize = 16f
        }

        codeInput = EditText(this).apply {
            hint = "123456"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            maxLines = 1
        }

        val connectButton = Button(this).apply {
            text = "Connect"

            setOnClickListener {
                connectUsingCode()
            }
        }

        statusText = TextView(this).apply {
            text = "Make sure your phone and computer are on the same network."
            textSize = 14f
        }

        layout.addView(title)
        layout.addView(subtitle)

        layout.addView(scanButton)

        layout.addView(divider)

        layout.addView(codeLabel)
        layout.addView(codeInput)
        layout.addView(connectButton)

        layout.addView(statusText)

        setContentView(layout)
    }

    private fun startQrScanner() {

        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan the QR code shown by DESK")
            setBeepEnabled(true)
            setOrientationLocked(false)
        }

        qrScanner.launch(options)
    }

    // -------------------------------------------------------------------------
    // QR pairing
    // -------------------------------------------------------------------------

    private fun handleQrPayload(payload: String) {

        try {
            val json = JSONObject(payload)

            if (json.optString("type") != "DESK_COMPANION_PAIR") {
                showStatus("This is not a DESK pairing QR code.")
                return
            }

            val version = json.optInt("version", 0)

            if (version != 1) {
                showStatus("Unsupported DESK pairing version.")
                return
            }

            val host = json.getString("host")
            val port = json.getInt("port")
            val code = json.getString("code")

            codeInput.setText(code)

            pairWithDesk(host, port, code)

        } catch (exception: Exception) {
            showStatus("Invalid DESK QR code.")
        }
    }

    // -------------------------------------------------------------------------
    // Manual pairing
    // -------------------------------------------------------------------------

    private fun connectUsingCode() {

        val code = codeInput.text.toString().trim()

        if (code.length != 6 || !code.all { it.isDigit() }) {
            showStatus("Enter a valid 6-digit pairing code.")
            return
        }

        showStatus("Looking for DESK on your local network...")

        executor.execute {

            try {

                val discovered = DeskDiscovery.discover()

                if (discovered == null) {

                    runOnUiThread {
                        showStatus(
                            "DESK was not found. Make sure DESK is running " +
                                "and both devices are on the same network."
                        )
                    }

                    return@execute
                }

                pairWithDesk(
                    discovered.host,
                    discovered.port,
                    code
                )

            } catch (exception: Exception) {

                runOnUiThread {
                    showStatus(
                        "Could not find DESK: " +
                            (exception.message ?: "unknown error")
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Pair with DESK
    // -------------------------------------------------------------------------

    private fun pairWithDesk(
        host: String,
        port: Int,
        code: String
    ) {

        println(
            "[Companion] pairWithDesk called: " +
                "host=$host port=$port code=$code"
        )

        showStatus("Connecting to DESK...")

        executor.execute {

            try {

                val url = URL(
                    "http://$host:$port/api/pair"
                )

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                val body = JSONObject().apply {
                    put("code", code)
                    put(
                        "device_name",
                        android.os.Build.MODEL
                    )
                }

                connection.outputStream.use { output ->
                    output.write(
                        body.toString().toByteArray()
                    )
                }

                val responseCode =
                    connection.responseCode

                val responseStream =
                    if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val response =
                    responseStream.bufferedReader().use {
                        it.readText()
                    }

                connection.disconnect()

                val json = JSONObject(response)

                if (!json.optBoolean("success", false)) {

                    val error =
                        json.optString(
                            "error",
                            "Pairing failed."
                        )

                    runOnUiThread {
                        showStatus(error)
                    }

                    return@execute
                }

                val data =
                    json.getJSONObject("device")

                val deviceId =
                    data.getString("device_id")

                val token =
                    data.getString("token")

                saveCredentials(
                    host,
                    port,
                    deviceId,
                    token
                )

                runOnUiThread {

                    Toast.makeText(
                        this,
                        "DESK connected",
                        Toast.LENGTH_SHORT
                    ).show()

                    buildConnectedScreen()
                }

            } catch (exception: Exception) {

                runOnUiThread {

                    showStatus(
                        "Connection failed: " +
                            (
                                exception.message
                                    ?: "unknown error"
                            )
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Connected screen
    // -------------------------------------------------------------------------

    private fun buildConnectedScreen() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48)
        }

        val title = TextView(this).apply {
            text = "DESK Companion"
            textSize = 28f
        }

        val connectionStatus = TextView(this).apply {
            text = "● Connected"
            textSize = 18f
        }

        val host =
            getSharedPreferences(
                "desk_companion",
                MODE_PRIVATE
            ).getString(
                "host",
                "Unknown"
            )

        val port =
            getSharedPreferences(
                "desk_companion",
                MODE_PRIVATE
            ).getInt(
                "port",
                8765
            )

        val connectionInfo = TextView(this).apply {
            text = "Connected to DESK at\n$host:$port"
            textSize = 16f
        }

        val continueButton = Button(this).apply {
            text = "Continue"

            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "Companion interface coming next.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val disconnectButton = Button(this).apply {
            text = "Disconnect / Re-pair"

            setOnClickListener {
                disconnect()
            }
        }

        layout.addView(title)
        layout.addView(connectionStatus)
        layout.addView(connectionInfo)

        layout.addView(continueButton)

        layout.addView(disconnectButton)

        setContentView(layout)
    }

    // -------------------------------------------------------------------------
    // Credentials
    // -------------------------------------------------------------------------

    private fun saveCredentials(
        host: String,
        port: Int,
        deviceId: String,
        token: String
    ) {

        getSharedPreferences(
            "desk_companion",
            MODE_PRIVATE
        )
            .edit()
            .putString("host", host)
            .putInt("port", port)
            .putString("device_id", deviceId)
            .putString("token", token)
            .apply()
    }

    private fun hasSavedCredentials(): Boolean {

        val preferences =
            getSharedPreferences(
                "desk_companion",
                MODE_PRIVATE
            )

        val host =
            preferences.getString("host", null)

        val deviceId =
            preferences.getString("device_id", null)

        val token =
            preferences.getString("token", null)

        return !host.isNullOrBlank() &&
            !deviceId.isNullOrBlank() &&
            !token.isNullOrBlank()
    }

    private fun disconnect() {

        getSharedPreferences(
            "desk_companion",
            MODE_PRIVATE
        )
            .edit()
            .clear()
            .apply()

        buildPairingScreen()

        Toast.makeText(
            this,
            "Disconnected from DESK",
            Toast.LENGTH_SHORT
        ).show()
    }

    // -------------------------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------------------------

    private fun showStatus(message: String) {

        runOnUiThread {
            statusText.text = message
        }
    }

    override fun onDestroy() {

        executor.shutdownNow()

        super.onDestroy()
    }
}
