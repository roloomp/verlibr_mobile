package com.example.verlibr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import com.example.verlibr.data.Profile
import com.example.verlibr.data.network.AuthService
import kotlinx.coroutines.launch

class AuthController {
    var showLogin by mutableStateOf(false)
        private set

    var showRegister by mutableStateOf(false)
        private set

    /** null — не залогинен, иначе — профиль */
    var profile by mutableStateOf<Profile?>(null)
        private set

    val isLoggedIn: Boolean get() = profile != null

    fun openLogin() { showLogin = true }
    fun closeLogin() { showLogin = false }

    fun openRegister() { showRegister = true }
    fun closeRegister() { showRegister = false }

    fun updateProfile(p: Profile?) { profile = p }
}

@Composable
fun rememberAuthController(): AuthController = remember { AuthController() }

@Composable
fun AuthDialogs(
    controller: AuthController,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    if (controller.showLogin) {
        LoginDialog(
            onDismiss = controller::closeLogin,
            onLogin = { email, password ->
                scope.launch {
                    val success = AuthService.login(email, password)
                    controller.closeLogin()
                    if (success) {
                        val profile = AuthService.getCurrentProfile()
                        controller.updateProfile(profile)
                        snackbarHostState.showSnackbar("Вы вошли как ${profile?.name ?: email}")
                    } else {
                        snackbarHostState.showSnackbar("Неверный email или пароль")
                    }
                }
            },
            onGoToRegister = {
                controller.closeLogin()
                controller.openRegister()
            }
        )
    }

    if (controller.showRegister) {
        RegisterDialog(
            onDismiss = controller::closeRegister,
            onRegister = { email, name, password ->
                scope.launch {
                    val result = AuthService.register(name, email, password)
                    controller.closeRegister()
                    if (result.isSuccess) {
                        controller.updateProfile(AuthService.getCurrentProfile())
                        snackbarHostState.showSnackbar("Аккаунт создан")
                    } else {
                        snackbarHostState.showSnackbar("Ошибка: ${result.exceptionOrNull()?.message}")
                    }
                }
            },
            onGoToLogin = {
                controller.closeRegister()
                controller.openLogin()
            }
        )
    }
}

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onGoToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthDialog(title = "Вход", onDismiss = onDismiss) {
        Text("Введите свои данные для входа в аккаунт", style = MaterialTheme.typography.bodyMedium)
        Field("Email *", email, { email = it }, "Ваш email", keyboardType = KeyboardType.Email)
        Field("Пароль *", password, { password = it }, "Ваш пароль", isPassword = true)
        Button(onClick = { onLogin(email, password) }, modifier = Modifier.fillMaxWidth()) {
            Text("Войти")
        }
        OutlinedButton(onClick = onGoToRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Зарегистрироваться")
        }
    }
}

@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    onRegister: (email: String, name: String, password: String) -> Unit,
    onGoToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    val passwordsMatch = password == confirmPassword && confirmPassword.isNotBlank()

    AuthDialog(title = "Создать аккаунт", onDismiss = onDismiss) {
        Field(
            "Email *", email, { email = it }, "Ваш email",
            isError = showErrors && email.isBlank(),
            keyboardType = KeyboardType.Email
        )
        Field(
            "Отображаемое имя *", name, { name = it }, "Ваш никнейм",
            isError = showErrors && name.isBlank()
        )
        Field(
            "Пароль *", password, { password = it }, "Ваш пароль",
            isError = showErrors && password.isBlank(),
            isPassword = true
        )
        Field(
            "Подтвердите пароль *", confirmPassword, { confirmPassword = it }, "Ваш пароль",
            isError = showErrors && !passwordsMatch,
            isPassword = true
        )
        Button(
            onClick = {
                showErrors = true
                if (email.isNotBlank() && name.isNotBlank() && password.isNotBlank() && passwordsMatch) {
                    onRegister(email, name, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать аккаунт")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Уже есть аккаунт? ", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onGoToLogin) { Text("Войти") }
        }
    }
}

@Composable
private fun AuthDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Text("✕") }
                }
                content()
            }
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = isError,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
