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
    onSolicitarPermissoes: () -> Unit,
    onCapturarSelfie: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Olá, ${user.nome}", style = MaterialTheme.typography.headlineSmall)
        Text("Matrícula: ${user.matricula}")

        OutlinedTextField(
            value = observacao,
            onValueChange = { observacao = it },
            label = { Text("Observação") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSolicitarPermissoes,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Solicitar permissões")
        }

        Button(
            onClick = onCapturarSelfie,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Capturar selfie")
        }

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
            Text("Bater ponto")
        }

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

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sair")
        }

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
