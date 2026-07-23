package com.example.bike_app_ui.Chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bike_app_ui.p2p.P2pNodeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUserName: String,
    onBackClick: () -> Unit
){
    val p2pManager = remember { P2pNodeManager() }
    val messages = remember { mutableStateListOf<ChatMeesage>() }
    var inputText by remember { mutableStateOf("") }

    // 部屋選択ダイアログ表示フラグ & バックエンドから取得した部屋リスト
    var showRoomDialog by remember { mutableStateOf(false) }
    var roomList by remember { mutableStateOf<List<RoomResponse>>(emptyList()) }
    var myCreatedRoomId by remember { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 💡 1. 部屋一覧を取得 (ChatApiClient.roomService を使用)
    fun refreshRooms() {
        coroutineScope.launch {
            try {
                roomList = ChatApiClient.service.fetchRooms()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 💡 2. 自分の部屋を登録 (ポートは P2P 用の 9090 を送信)
    fun hostMyRoom() {
        coroutineScope.launch {
            try {
                val createdRoom = ChatApiClient.service.createRooms(
                    RegisterRoomRequest(hostName = currentUserName, port = 9090)
                )
                myCreatedRoomId = createdRoom.id
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        // P2P サーバー起動 (9090)
        p2pManager.startNode(port = 9090)

        p2pManager.incomingMessages.collect { incomingMsg ->
            messages.add(incomingMsg)
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // 💡 3. 画面を離れる際、自分が作成した roomId を渡して削除
            myCreatedRoomId?.let { roomId ->
                coroutineScope.launch {
                    try {
                        ChatApiClient.service.deleteRoom(roomId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            p2pManager.stopNode()
        }
    }

    // 📡 部屋選択・作成ダイアログ
    if (showRoomDialog) {
        AlertDialog(
            onDismissRequest = { showRoomDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("参加可能な部屋")
                    IconButton(onClick = { refreshRooms() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "更新")
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (roomList.isEmpty()) {
                        Text("利用可能な部屋がありません。", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(roomList, key = { it.id }) { room ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // 💡 127.0.0.1 や localhost の場合は 10.0.2.2 に置き換える
                                            val rawIp = if (room.hostIp == "127.0.0.1" || room.hostIp == "localhost") {
                                                "10.0.2.2"
                                            } else {
                                                room.hostIp
                                            }

                                            // タップされた部屋の IP:Port へ接続
                                            val targetAddress = "$rawIp:${room.port}"

                                            println("🔗 接続試行先: $targetAddress") // ログ確認用
                                            p2pManager.connectToPeer(targetAddress)
                                            showRoomDialog = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "${room.hostName} の部屋", fontSize = 16.sp)
                                        Text(
                                            text = "接続先: ${room.hostIp}:${room.port}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hostMyRoom()
                        showRoomDialog = false
                    },
                    enabled = myCreatedRoomId == null
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (myCreatedRoomId != null) "部屋公開中" else "部屋を作成")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoomDialog = false }) {
                    Text("閉じる")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "P2P Chat Room", fontSize = 18.sp)
                        Text(
                            text = "My PeerId: ${p2pManager.myPeerId}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            refreshRooms()
                            showRoomDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "部屋を探す/作成"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("メッセージを入力...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val textToSend = inputText.trim()
                            inputText = ""

                            coroutineScope.launch {
                                val sentMsg = p2pManager.sendMessage(textToSend, currentUserName)
                                messages.add(sentMsg)
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "送信",
                        tint = Color.White
                    )
                }
            }
        }
    }
}