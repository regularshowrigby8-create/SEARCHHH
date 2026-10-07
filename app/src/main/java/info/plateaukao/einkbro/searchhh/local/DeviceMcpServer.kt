package info.plateaukao.einkbro.searchhh.local

import android.content.Context
import com.google.gson.Gson
import info.plateaukao.einkbro.searchhh.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import java.security.MessageDigest

/** Official MCP SDK over its documented SSE transport, not an imitation JSON-RPC API. */
class DeviceMcpServer(
    private val context: Context,
) {
    private val backend = LocalBackend.get(context)
    private val gson = Gson()
    private val http =
        embeddedServer(CIO, host = "127.0.0.1", port = 0) {
            intercept(ApplicationCallPipeline.Plugins) {
                val expected = LocalIdentity.load(this@DeviceMcpServer.context)?.token.orEmpty()
                val supplied =
                    call.request.headers[HttpHeaders.Authorization]
                        ?.removePrefix("Bearer ")
                        .orEmpty()
                if (call.request.headers[HttpHeaders.Origin] != null) {
                    call.respond(HttpStatusCode.Forbidden, "Browser origins are not allowed")
                    finish()
                    return@intercept
                }
                if (expected.length < 64 || !MessageDigest.isEqual(expected.toByteArray(), supplied.toByteArray())) {
                    call.respond(HttpStatusCode.Unauthorized, "Pair using the credential shown on the device")
                    finish()
                    return@intercept
                }
                if (call.request.httpMethod == HttpMethod.Post) {
                    val length = call.request.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                    if (length == null || length !in 1..65536) {
                        call.respond(HttpStatusCode.PayloadTooLarge, "A bounded Content-Length (1–65536) is required")
                        finish()
                        return@intercept
                    }
                }
            }
            routing {
                get("/health") { call.respondText(gson.toJson(backend.details()), ContentType.Application.Json) }
            }
            // Use the Application overload exercised by the SDK's own integration test:
            // GET /sse advertises POST /message?sessionId=... . Both are authenticated.
            mcp { createTools() }
        }
    var port: Int = 0
        private set

    fun start() {
        http.start(wait = false)
        port =
            runBlocking {
                http.engine
                    .resolvedConnectors()
                    .first()
                    .port
            }
    }

    fun stop() {
        http.stop(500, 1500)
    }

    private fun createTools(): Server {
        val server =
            Server(
                Implementation("searchhh-device", "0.4.0"),
                ServerOptions(capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))),
            )

        fun tool(
            name: String,
            description: String,
            properties: JsonObject =
                buildJsonObject {
                },
            required: List<String> = emptyList(),
            action: suspend (JsonObject) -> Any,
        ) {
            server.addTool(name, description, Tool.Input(properties = properties, required = required)) { request ->
                try {
                    CallToolResult(content = listOf(TextContent(gson.toJson(action(request.arguments)))))
                } catch (
                    e: Exception,
                ) {
                    CallToolResult(content = listOf(TextContent("Tool failed: ${e.message}")), isError = true)
                }
            }
        }

        fun fields(vararg names: String) = buildJsonObject { names.forEach { n -> putJsonObject(n) { put("type", "string") } } }
        tool("server_status", "Read this installation's server ID, provider connectivity and limits. Does not expose credentials.") {
            backend.details()
        }
        tool(
            "list_sources",
            "List the global portal-source catalog. Catalog membership is not a guarantee of current provider availability.",
        ) {
            backend.sources()
        }
        tool(
            "list_codebases",
            "Read all submitted crawling-related codebases, additions, source links, audit status and the four reviewed Android capability adapters.",
        ) {
            backend.codebases
        }
        tool(
            "start_search",
            "Start a bounded continuous public-web search on this device. One active session. No form submission.",
            buildJsonObject {
                putJsonObject("query") {
                    put("type", "string")
                    put("minLength", 2)
                    put("maxLength", 240)
                }
                putJsonObject("mode") {
                    put("type", "string")
                    put("enum", JsonArray(listOf(JsonPrimitive("opportunities"), JsonPrimitive("links"))))
                }
                putJsonObject("engines") {
                    put("type", "array")
                    putJsonObject("items") { put("type", "string") }
                    put("minItems", 1)
                    put("maxItems", 128)
                }
                putJsonObject("adapter") {
                    put("type", "string")
                    put(
                        "enum",
                        JsonArray(CrawlerAdapters.available.map { JsonPrimitive(it.id) }),
                    )
                }
            },
            listOf("query", "engines"),
        ) { args ->
            backend.start(
                StartRequest(
                    args
                        .getValue(
                            "query",
                        ).jsonPrimitive.content,
                    args["mode"]?.jsonPrimitive?.content ?: "opportunities",
                    args.getValue("engines").jsonArray.map {
                        it.jsonPrimitive.content
                    },
                    false,
                    args["adapter"]?.jsonPrimitive?.content,
                ),
            )
        }
        tool(
            "get_results",
            "Read a local search session, dedup statistics, source errors and unverified opportunities.",
            fields("id"),
            listOf("id"),
        ) {
            backend.status(it.getValue("id").jsonPrimitive.content)
        }
        tool(
            "stop_search",
            "Stop a local search. An in-flight network response may finish, but no new results will commit.",
            fields("id"),
            listOf("id"),
        ) {
            backend.stop(it.getValue("id").jsonPrimitive.content)
        }
        tool("list_saved", "List this installation's locally saved opportunities.") {
            SearchhhDatabase
                .get(context)
                .saved()
                .all()
                .first()
                .map { gson.fromJson(it.payload, Opportunity::class.java) }
        }
        tool(
            "save_result",
            "Save one existing session result locally. Does not open or submit a form.",
            fields("sessionId", "resultId"),
            listOf("sessionId", "resultId"),
        ) { args ->
            val result =
                backend.status(args.getValue("sessionId").jsonPrimitive.content).results.first {
                    it.id ==
                        args.getValue("resultId").jsonPrimitive.content
                }
            SearchhhDatabase.get(context).saved().save(SavedOpportunity(result.id, gson.toJson(result)))
            mapOf("saved" to result.id)
        }
        return server
    }
}
