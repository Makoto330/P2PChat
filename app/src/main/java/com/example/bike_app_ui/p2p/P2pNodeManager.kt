package com.example.bike_app_ui.p2p

import android.app.Application
import com.example.bike_app_ui.Chat.ChatMeesage
import io.ktor.client.engine.cio.CIO
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


class P2pNodeManager {

    val myPeerId: String = UUID.randomUUID().toString().take(8)

    var activeSession = ConcurrentHashMap<String, WebSocketSession>()

    private val _incomingMessage = MutableSharedFlow<ChatMeesage>()
    val incomingMessage: SharedFlow<ChatMeesage> = _incomingMessage.asSharedFlow()

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var EngineServer: ApplicationEngine? = null

    fun startNode(port: Int = 9090){
        scope.launch(Dispatchers.IO) {
            try {
                if (EngineServer != null) {
                    return@launch
                }
                EngineServer = embeddedServer(CIO, port = port, host = "0.0.0.0"){
                    install(io.ktor.server.websocket.WebSockets) {
                        pingPeriod = java.time.Duration.ofSeconds(15)
                    }

                    routing {
                        webSocket("/p2p") {
                            val remotePeer = UUID.randomUUID().toString().take(4)
                            activeSession[remotePeer] = this
                            try {
                                for (frame in incoming) {
                                    if (frame is Frame.Text){
                                        val text = frame.readText()
                                        val msg = ChatMeesage(
                                            senderPeerId = remotePeer,
                                            senderName = "Peer",
                                            text = text,
                                            isMine = false
                                        )
                                        _incomingMessage.emit(msg)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                EngineServer?.start(wait = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun connectToPeer(hostAndPort: String){
        scope.launch {
            try {
                val url = "ws://$hostAndPort/p2p"
                httpClient.webSockets(url) {
                    val peerId = "Peer-" + hostAndPort
                    activeSession[peerId] = this

                    for (frame in incoming) {
                        if (frame is Frame.Text){
                            val text = frame.readText()
                            val msg = ChatMeesage(
                                senderPeerId =  peerId,
                                senderName = "Peer",
                                text = text,
                                isMine = false
                            )
                            _incomingMessage.emit(msg)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    suspend fun sendMessage(text:String, senderName: String): ChatMeesage {
        val msg = ChatMeesage(
            senderPeerId = myPeerId,
            senderName = senderName,
            text = text,
            isMine = true
        )
        scope.launch {
            try {
                activeSession.values.forEach { session ->
                    session.send(Frame.Text(text))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return msg
    }

    fun stopNode(){

    }
}