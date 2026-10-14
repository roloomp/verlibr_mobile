package com.example.verlibr.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.verlibr.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.verlibr.data.network.AuthService
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.verlibr.data.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    onLogoClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clickable(onClick = onLogoClick)   // ← клик по всей группе иконка+текст
                    .padding(vertical = 4.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.flower),
                    contentDescription = "На главную",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Verlibr",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        actions = {
            TextButton(onClick = onLoginClick) {
                Text(
                    text = "Вход",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}



@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onGoToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Вход",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(R.drawable.flower), contentDescription = "Закрыть")
                    }
                }

                Text(
                    text = "Введите свои данные для входа в аккаунт",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))

                Text("Email *", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Ваш email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text("Пароль *", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Ваш пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onLogin(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Войти")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onGoToRegister,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Зарегистрироваться")
                }
            }
        }
    }
}

@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    onRegister: (email: String, name: String, password: String, confirmPassword: String) -> Unit,
    onGoToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Создать аккаунт",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(R.drawable.flower), contentDescription = "Закрыть")
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text("Email *", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                    },
                    placeholder = { Text("Ваш email") },
                    singleLine = true,
                    isError = emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Отображаемое имя *",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ваш никнейм",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    placeholder = { Text("Ваш никнейм") },
                    singleLine = true,
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text("Пароль *", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false
                    },
                    placeholder = { Text("Ваш пароль") },
                    singleLine = true,
                    isError = passwordError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text("Подтвердите пароль *", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmError = false
                    },
                    placeholder = { Text("Ваш пароль") },
                    singleLine = true,
                    isError = confirmError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        emailError = email.isBlank()
                        nameError = name.isBlank()
                        passwordError = password.isBlank()
                        confirmError = password != confirmPassword || confirmPassword.isBlank()

                        if (!emailError && !nameError && !passwordError && !confirmError) {
                            onRegister(email, name, password, confirmPassword)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Создать аккаунт")
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Уже есть аккаунт? ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onGoToLogin) {
                        Text("Войти")
                    }
                }
            }
        }
    }
}



@Composable
fun AuthDialogs(
    controller: AuthController,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    if (controller.showLogin) {
        LoginDialog(
            onDismiss = { controller.closeLogin() },
            onLogin = { email, password ->
                scope.launch {
                    val success = AuthService.login(email, password)
                    controller.closeLogin()
                    if (success) {
                        val profile = AuthService.getCurrentProfile()
                        controller.setProfile(profile)
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
            onDismiss = { controller.closeRegister() },
            onRegister = { email, name, password, _ ->
                scope.launch {
                    val result = AuthService.register(name, email, password)
                    controller.closeRegister()
                    if (result.isSuccess) {
                        val profile = AuthService.getCurrentProfile()
                        controller.setProfile(profile)
                        snackbarHostState.showSnackbar("Аккаунт создан")
                    } else {
                        snackbarHostState.showSnackbar(
                            "Ошибка: ${result.exceptionOrNull()?.message}"
                        )
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



class AuthController {
    var showLogin by mutableStateOf(false)
        internal set

    var showRegister by mutableStateOf(false)
        internal set

    /** null — не залогинен, иначе — профиль */
    var profile by mutableStateOf<Profile?>(null)
        internal set

    val isLoggedIn: Boolean get() = profile != null

    fun openLogin() { showLogin = true }
    fun closeLogin() { showLogin = false }

    fun openRegister() { showRegister = true }
    fun closeRegister() { showRegister = false }

    fun setProfile(p: Profile?) { profile = p }
}

@Composable
fun rememberAuthController(): AuthController = remember { AuthController() }