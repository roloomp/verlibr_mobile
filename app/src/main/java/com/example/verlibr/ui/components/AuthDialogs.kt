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
import kotlinx.coroutines.launch

class AuthController {
    var showLogin by mutableStateOf(false)
        private set

    var showRegister by mutableStateOf(false)
        private set

    /** Идёт запрос — кнопки в диалогах блокируются. */
    var busy by mutableStateOf(false)
        private set

    /** Текст ошибки, показывается прямо в диалоге (снекбар скрыт под окном диалога). */
    var error by mutableStateOf<String?>(null)
        private set

    fun openLogin() { error = null; showLogin = true }
    fun closeLogin() { showLogin = false; error = null }

    fun openRegister() { error = null; showRegister = true }
    fun closeRegister() { showRegister = false; error = null }

    fun changeBusy(value: Boolean) { busy = value }
    fun changeError(value: String?) { error = value }
}

@Composable
fun rememberAuthController(): AuthController = remember { AuthController() }

@Composable
fun AuthDialogs(
    controller: AuthController,
    snackbarHostState: SnackbarHostState,
    onLogin: suspend (email: String, password: String) -> Result<Unit>,
    onRegister: suspend (name: String, email: String, password: String) -> Result<Unit>
) {
    val scope = rememberCoroutineScope()

    if (controller.showLogin) {
        LoginDialog(
            error = controller.error,
            enabled = !controller.busy,
            onDismiss = controller::closeLogin,
            onLogin = { email, password ->
                if (email.isBlank() || password.isBlank()) {
                    controller.changeError("Введите email и пароль")
                } else if (!controller.busy) {
                    scope.launch {
                        controller.changeBusy(true)
                        controller.changeError(null)
                        val result = onLogin(email, password)
                        controller.changeBusy(false)
                        if (result.isSuccess) {
                            controller.closeLogin()
                            snackbarHostState.showSnackbar("Вы вошли в аккаунт")
                        } else {
                            controller.changeError(result.exceptionOrNull()?.message ?: "Не удалось войти")
                        }
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
            error = controller.error,
            enabled = !controller.busy,
            onDismiss = controller::closeRegister,
            onRegister = { email, name, password ->
                if (!controller.busy) {
                    scope.launch {
                        controller.changeBusy(true)
                        controller.changeError(null)
                        val result = onRegister(name, email, password)
                        controller.changeBusy(false)
                        if (result.isSuccess) {
                            controller.closeRegister()
                            snackbarHostState.showSnackbar("Аккаунт создан")
                        } else {
                            controller.changeError(result.exceptionOrNull()?.message ?: "Не удалось создать аккаунт")
                        }
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
    error: String?,
    enabled: Boolean,
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
        ErrorText(error)
        Button(
            onClick = { onLogin(email, password) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (enabled) "Войти" else "Подождите…")
        }
        OutlinedButton(onClick = onGoToRegister, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text("Зарегистрироваться")
        }
    }
}

@Composable
fun RegisterDialog(
    error: String?,
    enabled: Boolean,
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
        ErrorText(error)
        Button(
            enabled = enabled,
            onClick = {
                showErrors = true
                if (email.isNotBlank() && name.isNotBlank() && password.isNotBlank() && passwordsMatch) {
                    onRegister(email, name, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (enabled) "Создать аккаунт" else "Подождите…")
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
private fun ErrorText(error: String?) {
    if (error != null) {
        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
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
