package com.example.diploma

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.util.Log
import com.example.diploma.data.remote.ApiClient
import com.example.diploma.data.remote.ChatBotRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val BotBubbleBg = Color(0xFFF5F5F5)
private val UserBubbleBg = Color(0xFF0A73F0)
private val YellowAccent = Color(0xFFFDC725)
private val GrayText = Color(0xFF999999)
private const val CHAT_PREFS = "chatbot_saved_sessions"
private const val CHAT_KEY = "sessions_json"

private data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val time: String,
    val images: List<Int> = emptyList()
)

private data class SavedChatSession(
    val id: Long,
    val title: String,
    val lastMessage: String,
    val updatedAt: String,
    val messages: List<ChatMessage>
)

private fun currentTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private fun todayDateRu(): String =
    SimpleDateFormat("d MMMM", Locale("ru")).format(Date())

private suspend fun sendChatWithFallback(message: String, sessionId: Int?): ChatBotResult {
    val firstTry = runCatching {
        ApiClient.api.chatBot(
            ChatBotRequest(
                message = message,
                sessionId = sessionId
            )
        )
    }
    if (firstTry.isSuccess) {
        return ChatBotResult.Success(firstTry.getOrThrow())
    }

    val firstError = firstTry.exceptionOrNull()
    Log.w("ChatBot", "First try failed: ${firstError?.message}")

    // Retry with the same session once (temporary transport glitches)
    delay(250)
    val secondTry = runCatching {
        ApiClient.api.chatBot(
            ChatBotRequest(
                message = message,
                sessionId = sessionId
            )
        )
    }
    if (secondTry.isSuccess) {
        return ChatBotResult.Success(secondTry.getOrThrow())
    }

    val secondError = secondTry.exceptionOrNull()
    Log.w("ChatBot", "Second try failed: ${secondError?.message}")

    // Final fallback: reset server session if we had one
    if (sessionId != null) {
        delay(250)
        val thirdTry = runCatching {
            ApiClient.api.chatBot(ChatBotRequest(message = message, sessionId = null))
        }
        if (thirdTry.isSuccess) {
            return ChatBotResult.Success(thirdTry.getOrThrow())
        }
        val thirdError = thirdTry.exceptionOrNull()
        Log.w("ChatBot", "Third try (new session) failed: ${thirdError?.message}")
        return ChatBotResult.Failure(thirdError ?: secondError ?: firstError)
    }

    return ChatBotResult.Failure(secondError ?: firstError)
}

private sealed interface ChatBotResult {
    data class Success(val response: com.example.diploma.data.remote.ChatBotResponse) : ChatBotResult
    data class Failure(val error: Throwable?) : ChatBotResult
}

@Composable
fun ChatBotPage(navController: NavController) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var sessionId by rememberSaveable { mutableStateOf<Int?>(null) }
    val savedSessions = remember { mutableStateListOf<SavedChatSession>() }
    var currentSessionTitle by remember { mutableStateOf<String?>(null) }
    var currentSavedChatId by rememberSaveable { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(Unit) {
        savedSessions.clear()
        savedSessions.addAll(loadSavedSessions(ctx))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 18.dp)
                ) {
                    Text("Сохраненные чаты", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (savedSessions.isEmpty()) {
                        Text("Пока нет сохраненных чатов", fontSize = 13.sp, color = GrayText)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(savedSessions, key = { it.id }) { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF7F9FC))
                                        .border(1.dp, Color(0xFFE8EDF5), RoundedCornerShape(12.dp))
                                        .clickable {
                                            messages.clear()
                                            messages.addAll(item.messages)
                                            currentSessionTitle = item.title
                                            currentSavedChatId = item.id
                                            showAttachMenu = false
                                            scope.launch { drawerState.close() }
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            item.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Удалить чат",
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    val removedCurrent = currentSavedChatId == item.id
                                                    savedSessions.removeAll { it.id == item.id }
                                                    persistSavedSessions(ctx, savedSessions)
                                                    if (removedCurrent) {
                                                        messages.clear()
                                                        currentSavedChatId = null
                                                        currentSessionTitle = null
                                                        sessionId = null
                                                    }
                                                }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(item.lastMessage, fontSize = 12.sp, color = GrayText, maxLines = 2)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.updatedAt, fontSize = 11.sp, color = Color(0xFF9AA5B5))
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding)
                    .padding(top = 12.dp)
                    .imePadding()
            ) {
                ChatTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNewChatClick = {
                        currentSavedChatId = saveCurrentChatIfNeeded(
                            context = ctx,
                            messages = messages,
                            title = currentSessionTitle,
                            sessions = savedSessions,
                            currentId = currentSavedChatId
                        )
                        messages.clear()
                        inputText = ""
                        showAttachMenu = false
                        sessionId = null
                        currentSessionTitle = null
                        currentSavedChatId = null
                    },
                    onExitClick = {
                        currentSavedChatId = saveCurrentChatIfNeeded(
                            context = ctx,
                            messages = messages,
                            title = currentSessionTitle,
                            sessions = savedSessions,
                            currentId = currentSavedChatId
                        )
                        navController.navigate("ParentInsightsPage") {
                            popUpTo("ChatBotPage") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (messages.isEmpty() && !isLoading) {
                        Text(
                            "Чем могу помочь?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    todayDateRu(),
                                    fontSize = 13.sp,
                                    color = GrayText,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                            items(messages) { msg ->
                                ChatBubble(msg)
                            }
                            if (isLoading) {
                                item {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFF0A73F0)
                                        )
                                        Text("Печатает...", fontSize = 13.sp, color = GrayText)
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }

                    if (showAttachMenu) {
                        Box(modifier = Modifier.align(Alignment.BottomStart)) {
                            AttachmentMenu { showAttachMenu = false }
                        }
                    }
                }

                ChatInputBar(
                    text = inputText,
                    onTextChange = { inputText = it },
                    onSend = {
                        if (inputText.isNotBlank() && !isLoading) {
                            val userMsg = inputText.trim()
                            if (currentSessionTitle.isNullOrBlank()) {
                                currentSessionTitle = userMsg.take(32)
                            }
                            messages.add(ChatMessage(text = userMsg, isUser = true, time = currentTime()))
                            inputText = ""

                            scope.launch {
                                listState.animateScrollToItem(messages.size)
                                isLoading = true

                                val result = sendChatWithFallback(
                                    message = userMsg,
                                    sessionId = sessionId
                                )

                                isLoading = false

                                if (result is ChatBotResult.Success) {
                                    val response = result.response
                                    sessionId = response.sessionId
                                    messages.add(
                                        ChatMessage(
                                            text = response.reply ?: "Нет ответа",
                                            isUser = false,
                                            time = currentTime()
                                        )
                                    )
                                } else {
                                    val ex = (result as ChatBotResult.Failure).error
                                    Log.w("ChatBot", "Final failure: ${ex?.javaClass?.simpleName}: ${ex?.message}")
                                    messages.add(
                                        ChatMessage(
                                            text = "Временная ошибка соединения. Попробуйте еще раз.",
                                            isUser = false,
                                            time = currentTime()
                                        )
                                    )
                                }

                                currentSavedChatId = saveCurrentChatIfNeeded(
                                    context = ctx,
                                    messages = messages,
                                    title = currentSessionTitle,
                                    sessions = savedSessions,
                                    currentId = currentSavedChatId
                                )
                                listState.animateScrollToItem(messages.size)
                            }
                        }
                    },
                    onAttach = { showAttachMenu = !showAttachMenu }
                )
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chat_menu),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable { onMenuClick() },
            tint = YellowAccent
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.ic_chat_new),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable { onNewChatClick() },
            tint = YellowAccent
        )

        Text(
            "Чат Помощник",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Icon(
            painter = painterResource(R.drawable.ic_chat_signin),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable { onExitClick() },
            tint = YellowAccent
        )
    }
}

private fun loadSavedSessions(context: Context): List<SavedChatSession> {
    val prefs = context.getSharedPreferences(CHAT_PREFS, Context.MODE_PRIVATE)
    val raw = prefs.getString(CHAT_KEY, null) ?: return emptyList()
    return runCatching {
        val arr = JSONArray(raw)
        buildList {
            repeat(arr.length()) { i ->
                val obj = arr.optJSONObject(i) ?: return@repeat
                val msgsArr = obj.optJSONArray("messages") ?: JSONArray()
                val msgs = buildList {
                    repeat(msgsArr.length()) { j ->
                        val m = msgsArr.optJSONObject(j) ?: return@repeat
                        add(
                            ChatMessage(
                                text = m.optString("text"),
                                isUser = m.optBoolean("isUser"),
                                time = m.optString("time")
                            )
                        )
                    }
                }
                add(
                    SavedChatSession(
                        id = obj.optLong("id"),
                        title = obj.optString("title"),
                        lastMessage = obj.optString("lastMessage"),
                        updatedAt = obj.optString("updatedAt"),
                        messages = msgs
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun saveCurrentChatIfNeeded(
    context: Context,
    messages: List<ChatMessage>,
    title: String?,
    sessions: MutableList<SavedChatSession>,
    currentId: Long?
): Long? {
    if (messages.isEmpty()) return currentId
    val now = currentTime()
    val safeTitle = (title?.ifBlank { null } ?: messages.firstOrNull { it.isUser }?.text ?: "Новый чат").take(32)
    val last = messages.lastOrNull()?.text.orEmpty()
    val resolvedId = currentId ?: System.currentTimeMillis()
    val newItem = SavedChatSession(
        id = resolvedId,
        title = safeTitle,
        lastMessage = last,
        updatedAt = now,
        messages = messages.toList()
    )
    val dedup = sessions.filterNot { it.id == resolvedId }
    sessions.clear()
    sessions.add(0, newItem)
    sessions.addAll(dedup.take(29))

    persistSavedSessions(context, sessions)
    return resolvedId
}

private fun persistSavedSessions(context: Context, sessions: List<SavedChatSession>) {
    val arr = JSONArray()
    sessions.forEach { session ->
        val obj = JSONObject()
        obj.put("id", session.id)
        obj.put("title", session.title)
        obj.put("lastMessage", session.lastMessage)
        obj.put("updatedAt", session.updatedAt)
        val msgArr = JSONArray()
        session.messages.forEach { m ->
            val mObj = JSONObject()
            mObj.put("text", m.text)
            mObj.put("isUser", m.isUser)
            mObj.put("time", m.time)
            msgArr.put(mObj)
        }
        obj.put("messages", msgArr)
        arr.put(obj)
    }
    context.getSharedPreferences(CHAT_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(CHAT_KEY, arr.toString())
        .apply()
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (message.images.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                message.images.forEach { imgRes ->
                    Image(
                        painter = painterResource(imgRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) UserBubbleBg else BotBubbleBg)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                message.text,
                color = if (isUser) Color.White else Color.Black,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📋", fontSize = 14.sp)
                    Text("👍", fontSize = 14.sp)
                    Text("👎", fontSize = 14.sp)
                }
                Text(message.time, fontSize = 12.sp, color = GrayText)
            }
        } else {
            Text(
                message.time,
                fontSize = 12.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AttachmentMenu(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(vertical = 4.dp)
    ) {
        AttachmentItem(iconRes = R.drawable.ic_attach_camera, label = "Камера") { onDismiss() }
        AttachmentItem(iconRes = R.drawable.ic_attach_photo, label = "Фото") { onDismiss() }
        AttachmentItem(iconRes = R.drawable.ic_attach_file, label = "Файлы") { onDismiss() }
    }
}

@Composable
private fun AttachmentItem(iconRes: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F4F8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color(0xFF0A73F0),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAttach, modifier = Modifier.size(40.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color(0xFF555555))
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Запрос", color = Color(0xFFBBBBBB), fontSize = 13.sp) },
            modifier = Modifier
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            textStyle = TextStyle(fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE0E0E0),
                unfocusedBorderColor = Color(0xFFE8E8E8),
                focusedContainerColor = Color(0xFFF9F9F9),
                unfocusedContainerColor = Color(0xFFF9F9F9)
            ),
            singleLine = true
        )

        if (text.isBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chat_mic),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = YellowAccent
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(onClick = onSend, modifier = Modifier.size(40.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(UserBubbleBg),
                contentAlignment = Alignment.Center
            ) {
                Text("➤", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

