package com.example.kodomo_album.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kodomo_album.core.util.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                is UiEvent.ShowSnackbar -> onShowSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "子どもアルバム",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "家族の思い出を記録しよう",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
            label = { Text("パスワード") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility 
                                     else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "パスワードを隠す" else "パスワードを表示"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None 
                                 else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.onEvent(LoginEvent.Login) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("ログイン")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { viewModel.onEvent(LoginEvent.NavigateToPasswordReset) }
        ) {
            Text("パスワードを忘れた場合")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "アカウントをお持ちでない場合",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = { viewModel.onEvent(LoginEvent.NavigateToSignUp) }
            ) {
                Text("新規登録")
            }
        }
    }
}