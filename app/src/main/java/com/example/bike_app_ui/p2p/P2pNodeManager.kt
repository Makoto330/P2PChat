package com.example.bike_app_ui.p2p

import com.example.bike_app_ui.Chat.ChatMeesage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap



class P2pNodeManager {
    val myPeerId: String = "Peer-" + UUID.randomUUID().toString().take(8)

    private val activateSessions = ConcurrentHashMap<String, DefaultWebSocketSession>()

    private  val _incomingMessages = MutableSharedFlow<ChatMeesage>()
    val incomingMessages: SharedFlow<ChatMeesage> = _incomingMessages.asSharedFlow()

    private var serverEngine: ApplicationEngine? = null

    private val httpClient = HttpClient(io.ktor.client.engine.cio.CIO) {
        install(io.ktor.client.plugins.websocket.WebSockets)
    }

    private val nodeScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startNode(port: Int = 9090) {
        // 💡 Dispatchers.IO で明示的にバックグラウンド起動
        nodeScope.launch(Dispatchers.IO) {
            try {
                if (serverEngine != null) {
                    println("⚠️ Node is already running")
                    return@launch
                }

                // host = "0.0.0.0" を明示的に指定してバインドエラーを防ぐ
                serverEngine = embeddedServer(CIO, port = port, host = "0.0.0.0") {
                    install(io.ktor.server.websocket.WebSockets) {
                        pingPeriod = java.time.Duration.ofSeconds(15)
                    }
                    routing {
                        webSocket("/p2p") {
                            val remotePeer = "Peer-" + UUID.randomUUID().toString().take(4)
                            activateSessions[remotePeer] = this
                            println("🔗 New Peer Connected: $remotePeer")

                            try {
                                for (frame in incoming) {
                                    if (frame is Frame.Text) {
                                        val text = frame.readText()
                                        val msg = ChatMeesage(
                                            senderPeerId = remotePeer,
                                            senderName = "Peer",
                                            text = text,
                                            isMine = false
                                        )
                                        _incomingMessages.emit(msg)
                                    }
                                }
                            } catch (e: Exception) {
                                println("⚠️ Session Error: ${e.message}")
                            } finally {
                                activateSessions.remove(remotePeer)
                            }
                        }
                    }
                }

                // サーバー起動 (非同期)
                serverEngine?.start(wait = false)
                println("🚀 Android P2P Node Listening on port $port (My ID: $myPeerId)")

            } catch (e: Throwable) {
                // ❌ 万が一エラーが起きてもアプリを落とさずにログだけ吐く
                println("❌ P2P Node Start Error: ${e.localizedMessage}")
                e.printStackTrace()
                serverEngine = null
            }
        }
    }
    fun connectToPeer(hostAndPort: String) {
        nodeScope.launch {
            try {
                val url = "ws://$hostAndPort/p2p"
                println("🔗 Connecting to P2P Peer at $url...")

                httpClient.webSocket(url) {
                    val peerKey = "Peer-" + hostAndPort
                    activateSessions[peerKey] = this
                    println("✅ Connected to Peer: $peerKey")

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            val msg = ChatMeesage(
                                senderPeerId = peerKey,
                                senderName = "Peer",
                                text = text,
                                isMine = false
                            )
                            _incomingMessages.emit(msg)
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ Connection to peer failed: ${e.message}")
            }
        }
    }
    suspend fun sendMessage(text: String, senderName: String): ChatMeesage {
        val message = ChatMeesage(
            senderPeerId = myPeerId,
            senderName = senderName,
            text = text,
            isMine = true
        )

        activateSessions.values.forEach { session ->
            nodeScope.launch {
                try {
                    session.send(Frame.Text(text))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        println("📤 Broadcasted P2P Message to ${activateSessions.size} peers: $text")
        return message
    }

    fun stopNode() {
        serverEngine?.stop(1000, 2000)
        nodeScope.cancel()
        println("🛑 P2P Node Stopped")
    }
}