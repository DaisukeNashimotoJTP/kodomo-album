package com.example.kodomo_album.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kodomo_album.core.util.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordResetScreen(
    onNavigateUp: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: PasswordResetViewModel = hiltViewModel()
) {
    val state = viewModel.state

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateUp -> onNavigateUp()
                is UiEvent.ShowSnackbar -> onShowSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        IconButton(
            onClick = { viewModel.onEvent(PasswordResetEvent.NavigateToLogin) },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "戻る"
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "パスワードリセット",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = if (state.isEmailSent) {
                    "パスワードリセットメールを送信しました。\nメールをご確認ください。"
                } else {
                    "登録したメールアドレスを入力してください。\nパスワードリセット用のリンクをお送りします。"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (!state.isEmailSent) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(PasswordResetEvent.EmailChanged(it)) },
                    label = { Text("メールアドレス") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.onEvent(PasswordResetEvent.SendPasswordReset) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("リセットメールを送信")
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.onEvent(PasswordResetEvent.NavigateToLogin) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ログイン画面に戻る")
                }
            }
        }
    }
}