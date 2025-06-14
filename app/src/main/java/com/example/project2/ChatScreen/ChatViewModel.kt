package com.example.project2.ChatScreen

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.data.ChatItem
import com.example.project2.data.ChatState
import com.example.project2.data.DialogueRepository
import com.example.project2.data.UserSessionManager
import com.example.project2.data.database.Dialogue
import com.example.project2.data.network.ChatEffect
import com.example.project2.data.network.ChatIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject


//class ChatViewModel : ViewModel() {
//    //private val chatItemExam: ChatItem = ChatItem(id = 0, character = "bot", chatText = "Hello,How can I help you?")
//    private val _chatItems = mutableStateOf(listOf<ChatItem>())
//    val chatItems: State<List<ChatItem>> get() = _chatItems
//    private val chatItem by chatItems
//    private val count: Int get() = _chatItems.value.size
//
//    private val _isGenerating = MutableStateFlow(false)
//    val isGenerating: StateFlow<Boolean> = _isGenerating
//
//    fun addChatItem(character: String, chatText: String) {
//        val chatItem = ChatItem(id = count, character = character, chatText = chatText)
//        _chatItems.value = _chatItems.value + chatItem
//
//        sendChatRequest(prompt = chatItem.chatText)
//    }
//
//
//    private fun sendChatRequest(prompt: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                _isGenerating.value = true
//
//                val client = OkHttpClient.Builder()
//                    .connectTimeout(120, TimeUnit.SECONDS)
//                    .readTimeout(120, TimeUnit.SECONDS)
//                    .writeTimeout(120, TimeUnit.SECONDS)
//                    .build()
//                val jsonObject = org.json.JSONObject().apply {
//                    put("query", prompt)
//                    put("top_k", 4)
//                }
//
//                val requestBody = jsonObject.toString()
//                    .toRequestBody("application/json".toMediaTypeOrNull())
//
//                val request = okhttp3.Request.Builder()
//                    .url("https://rag.zongzi.org/api/v1/chat")
//                    .post(requestBody)
//                    .build()
//
//                val response = client.newCall(request).execute()
//
//                if (response.isSuccessful) {
//                    val bodyString = response.body?.string()
//                    val json = org.json.JSONObject(bodyString ?: "")
//                    val answer = json.optString("response", "[无回答]")
//
//                    val chatItem = ChatItem(
//                        id = System.currentTimeMillis().toInt(),
//                        character = "bot",
//                        chatText = answer.trim()
//                    )
//                    _chatItems.value = _chatItems.value + chatItem
//                } else {
//                    val chatItem = ChatItem(id = count, character = "bot", chatText = "服务器响应失败")
//                    _chatItems.value = _chatItems.value + chatItem
//                }
//
//            } catch (e: Exception) {
//                Log.e("ChatViewModel", "网络异常: ${e.message}", e)  // ← 打印异常日志
//                val chatItem = ChatItem(id = count, character = "bot", chatText = "network error")
//                _chatItems.value = _chatItems.value + chatItem
//            } finally {
//                _isGenerating.value = false
//            }
//        }
//
////    fun getChatItemFromInternet(){
////        try {
////            viewModelScope.launch {
////                fun NetworkChatItem.toChatItem(): ChatItem {
////                    return ChatItem(id = 0 , character = "bot", chatText = this.chatText)
////                }
////
////                val result = ChatAPI.retrofitService.getNetworkChatItem()
////                val resultData = result.map { it.toChatItem() }
////
////                _chatItems.value = resultData
////            }
////        }catch (e: IOException){
////
////        }
//
//    }
//}

class ChatRepository @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun sendChatRequest(prompt: String): String = withContext(Dispatchers.IO) {
        val jsonObject = org.json.JSONObject().apply {
            put("query", prompt)
            put("top_k", 4)
        }

        val requestBody = jsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = okhttp3.Request.Builder()
            //.url("https://rag.zongzi.org/api/v1/chat")
            .url("http://192.168.31.9:6006/api/v1/chat")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val bodyString = response.body?.string()
            val json = org.json.JSONObject(bodyString ?: "")
            json.optString("response", "[无回答]")
        } else {
            throw Exception("服务器响应失败")
        }
    }
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val dialogueRepository: DialogueRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ChatEffect>()
    val effect: SharedFlow<ChatEffect> = _effect.asSharedFlow()

    // 当前会话的对话监听
    private var currentSessionJob: Job? = null

    fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.LoadSession -> {
                loadSession(intent.sessionId)
            }
            is ChatIntent.SendMessage -> {
                sendMessage(intent.message)
            }
            is ChatIntent.RetryLastMessage -> {
                retryLastMessage(intent.sessionId)
            }
            is ChatIntent.ClearSession -> {
                clearSession(intent.sessionId)
            }
        }
    }

    init {
        UserSessionManager.selectedSessionId?.let { sessionId ->
            loadSession(sessionId)
        }
    }

    private fun loadSession(sessionId: Int) {
        updateState { it.copy(currentSessionId = sessionId, isLoading = true) }

        // 取消之前的会话监听
        currentSessionJob?.cancel()

        // 开始监听新会话的对话
        currentSessionJob = viewModelScope.launch {
            dialogueRepository.getDialoguesBySession(sessionId)
                .catch { error ->
                    updateState {
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
                .collect { dialogues ->
                    updateState {
                        it.copy(
                            dialogues = dialogues,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun sendMessage(message: String) {
        val currentState = _state.value
        if (currentState.currentSessionId == 0) {
            updateState { it.copy(error = "请先选择或创建会话") }
            return
        }

        viewModelScope.launch {
            try {
                updateState { it.copy(isGenerating = true, error = null) }

                // 保存用户消息到数据库
                val userDialogue = Dialogue(
                    sessionId = currentState.currentSessionId,
                    role = "user",
                    message = message,
                    timestamp = System.currentTimeMillis()
                )

                dialogueRepository.insertDialogue(userDialogue)

                // 发送网络请求
                val response = chatRepository.sendChatRequest(message)

                // 保存Bot回复到数据库
                val botDialogue = Dialogue(
                    sessionId = currentState.currentSessionId,
                    role = "bot",
                    message = response.trim(),
                    timestamp = System.currentTimeMillis()
                )

                dialogueRepository.insertDialogue(botDialogue)

            } catch (e: Exception) {
                Log.e("ChatViewModel", "发送消息失败: ${e.message}", e)

                updateState { it.copy(error = e.message) }
            } finally {
                updateState { it.copy(isGenerating = false) }
            }
        }
    }

    private fun retryLastMessage(sessionId: Int) {
        viewModelScope.launch {
            try {
                val lastDialogue = dialogueRepository.getLatestDialogue(sessionId)
                if (lastDialogue != null && lastDialogue.role == "user") {
                    sendMessage(lastDialogue.message)
                }
            } catch (e: Exception) {
                updateState { it.copy(error = "重试失败: ${e.message}") }
            }
        }
    }

    private fun clearSession(sessionId: Int) {
        viewModelScope.launch {
            try {
                dialogueRepository.deleteDialoguesBySession(sessionId)
                updateState { it.copy(error = null) }
            } catch (e: Exception) {
                updateState { it.copy(error = "清除会话失败: ${e.message}") }
            }
        }
    }

    private fun updateState(update: (ChatState) -> ChatState) {
        _state.value = update(_state.value)
    }

    override fun onCleared() {
        super.onCleared()
        currentSessionJob?.cancel()
    }
}