package com.saksham.deskcompanion

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class DeskAgent(
    val name: String,
    val color: String,
    val backend: String,
    val model: String,
    val responseLength: String,
    val chromeProfile: String?,
    val systemPrompt: String? = null
)

data class WorkspaceItem(
    val name: String,
    val path: String,
    val type: String,
    val size: Long
)

data class AgentArtifacts(
    val agent: String,
    val artifacts: JSONArray
)

data class ChatResponse(
    val agent: String,
    val response: String,
    val backend: String,
    val model: String
)

class DeskApi(
    private val host: String,
    private val port: Int,
    private val deviceId: String,
    private val token: String
) {

    private fun request(
        method: String,
        path: String,
        body: JSONObject? = null
    ): JSONObject {

        val url = URL("http://$host:$port$path")

        val connection =
            url.openConnection() as HttpURLConnection

        connection.requestMethod = method
        connection.connectTimeout = 5000
        connection.readTimeout = 10000
        connection.useCaches = false

        connection.setRequestProperty(
            "X-DESK-Device-ID",
            deviceId
        )

        connection.setRequestProperty(
            "X-DESK-Token",
            token
        )

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        if (body != null) {

            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.outputStream.use { output ->
                output.write(
                    body.toString()
                        .toByteArray(StandardCharsets.UTF_8)
                )
            }
        }

        val responseCode =
            connection.responseCode

        val stream =
            if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

        val response =
            stream?.bufferedReader()?.use {
                it.readText()
            } ?: ""

        connection.disconnect()

        if (response.isBlank()) {
            throw Exception(
                "DESK returned an empty response ($responseCode)"
            )
        }

        val json = JSONObject(response)

        if (!json.optBoolean("success", false)) {

            throw Exception(
                json.optString(
                    "error",
                    "DESK request failed"
                )
            )
        }

        return json
    }

    fun getStatus(): JSONObject {
        return request(
            "GET",
            "/api/status"
        ).getJSONObject("data")
    }

    fun getAgents(): List<DeskAgent> {

        val data =
            request(
                "GET",
                "/api/agents"
            ).getJSONArray("data")

        val agents = mutableListOf<DeskAgent>()

        for (i in 0 until data.length()) {

            val item = data.getJSONObject(i)

            agents.add(
                DeskAgent(
                    name = item.optString("name"),
                    color = item.optString(
                        "color",
                        "#5B7FA6"
                    ),
                    backend = item.optString("backend"),
                    model = item.optString("model"),
                    responseLength = item.optString(
                        "response_length",
                        "standard"
                    ),
                    chromeProfile =
                        if (
                            item.isNull("chrome_profile")
                        ) {
                            null
                        } else {
                            item.optString(
                                "chrome_profile"
                            )
                        }
                )
            )
        }

        return agents
    }

    fun getModels(): JSONObject {
        return request(
            "GET",
            "/api/models"
        ).getJSONObject("data")
    }

    fun getWorkspace(): List<WorkspaceItem> {

        val data =
            request(
                "GET",
                "/api/workspace"
            ).getJSONObject("data")

        val items =
            data.getJSONArray("items")

        val result =
            mutableListOf<WorkspaceItem>()

        for (i in 0 until items.length()) {

            val item =
                items.getJSONObject(i)

            result.add(
                WorkspaceItem(
                    name = item.optString("name"),
                    path = item.optString("path"),
                    type = item.optString(
                        "type",
                        "file"
                    ),
                    size = item.optLong("size", 0)
                )
            )
        }

        return result
    }


    fun getWorkspaceFile(
        filePath: String
    ): String {

        val encodedPath =
            URLEncoder.encode(
                filePath,
                "UTF-8"
            ).replace("+", "%20")

        return request(
            "GET",
            "/api/workspace/file/$encodedPath"
        )
            .getJSONObject("data")
            .optString("content")
    }


    fun getAgent(
        agentName: String
    ): DeskAgent {

        val encoded =
            URLEncoder.encode(
                agentName,
                "UTF-8"
            ).replace("+", "%20")

        val data =
            request(
                "GET",
                "/api/agents/$encoded"
            )
                .getJSONObject("data")

        return DeskAgent(
            name = data.optString("name"),
            color = data.optString(
                "color",
                "#5B7FA6"
            ),
            backend = data.optString("backend"),
            model = data.optString("model"),
            responseLength =
                data.optString(
                    "response_length",
                    "standard"
                ),
            chromeProfile =
                if (data.isNull("chrome_profile")) {
                    null
                } else {
                    data.optString("chrome_profile")
                },
            systemPrompt =
                data.optString(
                    "system_prompt",
                    ""
                )
        )
    }

    fun sendChat(
        agentName: String,
        message: String,
        images: List<Pair<String, String>> = emptyList()
    ): ChatResponse {

        val body = JSONObject().apply {
            put("agent", agentName)
            put("message", message)

            val imageArray = JSONArray()

            for ((mime, data) in images) {
                imageArray.put(
                    JSONObject().apply {
                        put("mime", mime)
                        put("data", data)
                    }
                )
            }

            put("images", imageArray)
        }

        val data = request(
            "POST",
            "/api/chat",
            body
        ).getJSONObject("data")

        return ChatResponse(
            agent = data.optString("agent"),
            response = data.optString("response"),
            backend = data.optString("backend"),
            model = data.optString("model")
        )
    }
        

    fun getMemory(
        agentName: String
    ): String {

        val encoded =
            URLEncoder.encode(
                agentName,
                "UTF-8"
            ).replace("+", "%20")

        return request(
            "GET",
            "/api/agents/$encoded/memory"
        )
            .getJSONObject("data")
            .optString("content")
    }

    fun getArtifacts(
        agentName: String
    ): JSONArray {

        val encoded =
            URLEncoder.encode(
                agentName,
                "UTF-8"
            ).replace("+", "%20")

        return request(
            "GET",
            "/api/agents/$encoded/artifacts"
        )
            .getJSONArray("data")
    }
    
    
	fun getAllArtifacts(): List<AgentArtifacts> {
		val data = request(
			"GET",
			"/api/artifacts"
		).getJSONArray("data")

		val result = mutableListOf<AgentArtifacts>()

		for (i in 0 until data.length()) {
			val item = data.getJSONObject(i)

			result.add(
				AgentArtifacts(
					agent = item.optString("agent"),
					artifacts = item.optJSONArray("artifacts")
						?: JSONArray()
				)
			)
		}

		return result
}

    fun createAgent(
        name: String,
        systemPrompt: String,
        backend: String,
        model: String,
        color: String = "#5B7FA6",
        responseLength: String = "standard",
        chromeProfile: String? = null
    ): DeskAgent {

        val body =
            JSONObject().apply {

                put("name", name)
                put(
                    "system_prompt",
                    systemPrompt
                )
                put("backend", backend)
                put("model", model)
                put("color", color)
                put(
                    "response_length",
                    responseLength
                )

                if (chromeProfile == null) {
                    put(
                        "chrome_profile",
                        JSONObject.NULL
                    )
                } else {
                    put(
                        "chrome_profile",
                        chromeProfile
                    )
                }
            }

        val data =
            request(
                "POST",
                "/api/agents",
                body
            )
                .getJSONObject("data")

        return DeskAgent(
            name = data.optString("name"),
            color = data.optString(
                "color",
                "#5B7FA6"
            ),
            backend = data.optString("backend"),
            model = data.optString("model"),
            responseLength =
                data.optString(
                    "response_length",
                    "standard"
                ),
            chromeProfile =
                if (data.isNull("chrome_profile")) {
                    null
                } else {
                    data.optString("chrome_profile")
                }
        )
    }
}
