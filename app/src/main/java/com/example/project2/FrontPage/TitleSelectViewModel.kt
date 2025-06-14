package com.example.project2.FrontPage

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.data.SessionRepository
import com.example.project2.data.UserSessionManager
import com.example.project2.data.database.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// LoginViewModel 用于存储当前用户的登录信息，完成注册、登录功能的封装
@HiltViewModel
class TitleSelectViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _sessionId = MutableLiveData<Int>()
    val sessionId: LiveData<Int> get() = _sessionId

    private val _sessionsByUser = MutableStateFlow<List<Session>>(emptyList())
    val sessionsByUser: StateFlow<List<Session>> = _sessionsByUser

    init {
        UserSessionManager.userId?.let { userId ->
            getSessions(userId)
        }
    }

    fun insertSession(title: String, onSuccess: () -> Unit, onFailure: (String) -> Unit){
        val userId = UserSessionManager.userId
        val newSession =
            userId?.let { Session(userId = it, lastUsedTime = System.currentTimeMillis(), title = title) }
        viewModelScope.launch {
            try {
                _sessionId.value = newSession?.let { sessionRepository.insertSession(it).toInt() }
                UserSessionManager.updateSelectedSession(_sessionId.value)
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                onFailure("项目已存在")
                Log.d("insertSession", "error: 项目已存在")
            } catch (e: Exception) {
                onFailure("添加失败：" + e.localizedMessage)
                Log.d("insertSession", "添加失败： + ${e.localizedMessage}")
            }
        }
        if (userId != null) {
            getSessions(userId)
        }
    }

    private fun getSessions(userId: Int){
        viewModelScope.launch{
            _sessionsByUser.value = sessionRepository.getSessionsByUser(userId)
        }
    }

    fun selectSession(session: Session){
        _sessionId.value = session.id
       UserSessionManager.updateSelectedSession(_sessionId.value)
    }
}