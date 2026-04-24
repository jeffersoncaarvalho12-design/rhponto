package com.technet.rhponto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.technet.rhponto.model.AppUser

@Composable
fun HomeScreen(
    user: AppUser,
    onBaterPonto: (String, (String?, String?) -> Unit) -> Unit,
    onHistorico: ((String?, String?) -> Unit) -> Unit,
    onLogout: () -> Unit
) {
    var observacao by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf<String?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Olá,", style = MaterialTheme.typography.titleLarge)
        Text(user.nome, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Matrícula: ${user.matricula}")
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = observacao,
            onValueChange = { observacao = it },
            label = { Text("Observação opcional") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                mensagem = null
                erro = null
                onBaterPonto(observacao) { ok, err ->
                    mensagem = ok
                    erro = err
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Bater ponto com selfie + GPS")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                mensagem = null
                erro = null
                onHistorico { ok, err ->
                    mensagem = ok
                    erro = err
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver histórico")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sair")
        }

        Spacer(Modifier.height(16.dp))

        if (mensagem != null) {
            Card {
                Text(
                    text = mensagem ?: "",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (erro != null) {
            Card {
                Text(
                    text = erro ?: "",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
