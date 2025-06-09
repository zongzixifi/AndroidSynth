package com.example.project2.data

object UserSessionManager {
    private var _userId: Int? = null
    private var _selectedSessionId: Int? = null

    val userId: Int? get() = _userId
    val selectedSessionId: Int? get() = _selectedSessionId

    fun isLoggedIn(): Boolean = _userId != null

    fun saveUserId(userId: Int){
        _userId = userId
    }

    fun updateSelectedSession(sessionId: Int?) {
        _selectedSessionId = sessionId
    }

    fun clearSession() {
        _userId = null
        _selectedSessionId = null
    }
}