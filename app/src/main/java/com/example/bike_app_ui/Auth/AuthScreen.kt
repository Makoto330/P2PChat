package com.example.bike_app_ui.Auth

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalSubclassOptIn::class)
@Preview(showBackground = true)
@Composable
fun AuthScreen(
    onLoginSuccess: (UserResponse) -> Unit
){
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = if (isSignUp) "新規ユーザー登録" else "ログイン",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        errorMessage?.let { msg->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        AnimatedVisibility(visible = isSignUp) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("お名前") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("メールアドレス") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("パスワード") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        if (isSignUp) {
                            val user = AuthApiClient.service.register(AuthRequest(email,password,name))
                            onLoginSuccess(user)
                        } else {
                            val response = AuthApiClient.service.login(AuthRequest(email,password,name))
                            onLoginSuccess(response.user)
                        }
                    } catch (e: Exception) {
                        Log.e("MY_APP_DEBUG", "ログイン/登録エラーが発生しました", e)
                        errorMessage = if (isSignUp) "登録に失敗しました（重複メール等）" else "ログインに失敗しました（認証エラー）"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text(text = if (isSignUp) "登録する" else "ログイン", fontSize = 16.sp)
            }
        }

        TextButton(
            onClick = {
                isSignUp = !isSignUp
                errorMessage = null
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = if (isSignUp) "すでにアカウントをお持ちの方（ログインへ）" else "初めての方はこちら（新規アカウント作成）"
            )
        }


    }

}