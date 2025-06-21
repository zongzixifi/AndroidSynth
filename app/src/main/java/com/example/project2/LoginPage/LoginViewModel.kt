package com.example.project2.LoginPage

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.data.UserRepository
import com.example.project2.data.UserSessionManager
import com.example.project2.data.database.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// LoginViewModel 用于存储当前用户的登录信息，完成注册、登录功能的封装
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> get() = _userId

    fun setUsername(name: String) {
        _username.value = name
    }

    fun setPassword(pw: String) {
        _password.value = pw
    }

    fun registerUser(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val name = _username.value ?: return
        val pw = _password.value ?: return
        val newUser = User(username = name, password = pw, createdAt = System.currentTimeMillis())
        viewModelScope.launch {
            try {
                userRepository.insertUser(newUser)
                val insertedUser = userRepository.checkUser(name, pw)
                if (insertedUser != null) {
                    _userId.value = insertedUser.id.toString()
                    UserSessionManager.saveUserId(insertedUser.id)
                }
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                onFailure("用户名已存在")
            } catch (e: Exception) {
                onFailure("注册失败：" + e.localizedMessage)
            }
        }
    }

    fun loginUser(onSuccess: () -> Unit, onFailure: () -> Unit) {
        val name = _username.value ?: return
        val pw = _password.value ?: return
        viewModelScope.launch {
            val user = userRepository.checkUser(name, pw)
            if (user != null) {
                _userId.value = user.id.toString()
                UserSessionManager.saveUserId(user.id)
                onSuccess() // 登录成功
            } else {
                onFailure() // 登录失败
            }
        }
    }
}