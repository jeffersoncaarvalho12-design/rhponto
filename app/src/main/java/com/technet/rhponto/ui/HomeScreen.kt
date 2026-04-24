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
        Text("Para bater ponto, permita GPS e câmera, capture a selfie e depois registre o ponto.")

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
            Text("1. Permitir câmera e localização")
        }

        Button(
            onClick = {
                mensagem = "Selfie capturada. Agora toque em bater ponto."
                erro = null
                onCapturarSelfie()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("2. Capturar selfie facial")
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
            Text("3. Bater ponto com selfie + GPS")
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
