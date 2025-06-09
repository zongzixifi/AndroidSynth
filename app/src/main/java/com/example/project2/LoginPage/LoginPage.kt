package com.example.project2.LoginPage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2.R
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onClickJumpFrontScreen: () -> Unit = {}
) {
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showRegisterErrorDialog = remember { mutableStateOf(false) }
    val showLoginErrorDialog = remember { mutableStateOf(false) }
    val showFormatErrorDialog = remember { mutableStateOf(false) }
    val errorText = remember { mutableStateOf("") }

    val isFormValid = username.value.isNotBlank() && password.value.isNotBlank()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        // 标题
        Text(
            text = "音乐之旅，\n即刻开始。",
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32), // 深绿色
            textAlign = TextAlign.Start,
            modifier = Modifier
                .align(alignment = Alignment.Start)
                .weight(5F)
        )


        // 邮箱/电话输入框
        Column(
            modifier = Modifier.weight(2F)
        ) {
            RoundedInputField(
                value = username.value,
                placeholder = "用户名",
                onValueChange = {
                    username.value = it
                    viewModel.setUsername(username.value)
                                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 密码输入框
            RoundedInputField(
                value = password.value,
                placeholder = "密码",
                onValueChange = {
                    password.value = it
                    viewModel.setPassword(password.value)
                                },
                isPassword = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (showRegisterErrorDialog.value){
            Text(
                text = errorText.value,
                color = Color.Red
            )
        }
        if (showLoginErrorDialog.value){
            Text(
                text = "登录失败",
                color = Color.Red
            )
        }
        if (showFormatErrorDialog.value){
            Text(
                text = "用户名或密码未输入",
                color = Color.Red
            )
        }

        // 注册 & 继续 按钮
        Row(
            modifier = Modifier.fillMaxWidth().weight(2F),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = {
                    showLoginErrorDialog.value = false
                    showFormatErrorDialog.value = false
                    showRegisterErrorDialog.value = false
                    if(isFormValid){
                        viewModel.registerUser(
                            onSuccess = {
                                onClickJumpFrontScreen()
                            },
                            onFailure = {text ->
                                errorText.value = text
                                showRegisterErrorDialog.value = true
                            }
                        )
                    }
                    else{
                        showFormatErrorDialog.value = true
                    }
                },
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, Color(0xFF2E7D32)),
                modifier = Modifier
                    .weight(1.0F)
                    .height(50.dp)
            ) {
                Text(text = "注册", color = Color(0xFF2E7D32))
            }

            Spacer(Modifier.width(5.dp))

            Button(
                onClick = {
                    showRegisterErrorDialog.value = false
                    showLoginErrorDialog.value = false
                    showFormatErrorDialog.value = false
                    if (isFormValid){
                        viewModel.loginUser(
                            onSuccess = {
                                onClickJumpFrontScreen()
                            },
                            onFailure = {
                                showLoginErrorDialog.value = true
                            }
                        )
                    }else {
                        showFormatErrorDialog.value = true
                    }

                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier
                    .weight(1.0F)
                    .height(50.dp)
            ) {
                Text(text = "登录", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 第三方登录图标
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().weight(2F)
        ) {
            listOf("google", "microsoft", "github", "apple").forEach { iconName ->
                Icon(
                    painter = painterResource(id = getIconRes(iconName)),
                    contentDescription = iconName,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.2f))
                        .padding(8.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

// 圆角输入框
@Composable
fun RoundedInputField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        shape = RoundedCornerShape(50),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .fillMaxWidth()
    )
}

// 获取图标资源
@Composable
fun getIconRes(name: String): Int {
    return when (name) {
        "google" -> R.drawable.google
        "microsoft" -> R.drawable.microsoft
        "github" -> R.drawable.github
        "apple" -> R.drawable.apple_fill
        else -> R.drawable.ic_launcher_foreground
    }
}

@Preview
@Composable
private fun LoginScreenPrev() {
    LoginScreen()
}