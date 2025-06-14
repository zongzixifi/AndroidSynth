package com.example.project2.data.network

sealed class ChatIntent {
    data class LoadSession(val sessionId: Int) : ChatIntent()
    data class SendMessage(val message: String) : ChatIntent()
    data class RetryLastMessage(val sessionId: Int) : ChatIntent()
    data class ClearSession(val sessionId: Int) : ChatIntent()
}

sealed class ChatEffect {
    data class ShowToast(val message: String) : ChatEffect()
    data class ScrollToBottom(val smooth: Boolean = true) : ChatEffect()
    data class ShowError(val error: String) : ChatEffect()
    object HideKeyboard : ChatEffect()
    data class NavigateToSettings(val sessionId: Int) : ChatEffect()
    data class CopyToClipboard(val text: String) : ChatEffect()
}