package com.technet.rhponto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    savedLogin: String,
    hasSavedCredentials: Boolean,
    onPrimeiroAcesso: () -> Unit,
    onBiometricLogin: ((String?) -> Unit) -> Unit,
    onLogin: (String, String, Boolean, (String?) -> Unit) -> Unit
) {
    var login by remember { mutableStateOf(savedLogin) }
    var senha by remember { mutableStateOf("") }
    var salvarAcesso by remember { mutableStateOf(savedLogin.isNotBlank()) }
    var erro by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("RH Ponto Mobile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Registro de ponto com foto e localização")
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("CPF ou login mobile") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Row {
            Checkbox(
                checked = salvarAcesso,
                onCheckedChange = { salvarAcesso = it }
            )
            Text("Salvar acesso neste aparelho")
        }

        Spacer(Modifier.height(16.dp))

        if (erro != null) {
            Text(
                text = erro ?: "",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = {
                carregando = true
                erro = null
                onLogin(login, senha, salvarAcesso) { error ->
                    carregando = false
                    erro = error
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !carregando
        ) {
            Text(if (carregando) "Entrando..." else "Entrar")
        }

        Spacer(Modifier.height(12.dp))

        if (hasSavedCredentials) {
            Button(
                onClick = {
                    erro = null
                    onBiometricLogin { error ->
                        erro = error
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar com biometria do celular")
            }

            Spacer(Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = onPrimeiroAcesso,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Primeiro acesso")
        }
    }
}
