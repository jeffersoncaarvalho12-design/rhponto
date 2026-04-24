package com.technet.rhponto.data

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.technet.rhponto.model.AppUser
import com.technet.rhponto.model.BaterPontoRequest
import com.technet.rhponto.model.HistoricoItem
import com.technet.rhponto.model.HistoricoRequest
import com.technet.rhponto.model.LoginRequest
import com.technet.rhponto.model.PrimeiroAcessoRequest
import com.technet.rhponto.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class MobileRepository(val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://200.106.207.13:27005/api/mobile/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val api = retrofit.create(ApiService::class.java)

    fun login(
        login: String,
        senha: String,
        onSuccess: (AppUser) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp = api.login(LoginRequest(login = login, senha = senha))

                if (resp.ok && resp.funcionario != null) {
                    val user = AppUser(
                        id = resp.funcionario.id,
                        nome = resp.funcionario.nome,
                        matricula = resp.funcionario.matricula,
                        status = resp.funcionario.status,
                        loginMobile = resp.funcionario.loginMobile,
                        senha = senha
                    )
                    CoroutineScope(Dispatchers.Main).launch { onSuccess(user) }
                } else {
                    CoroutineScope(Dispatchers.Main).launch { onError(resp.message) }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onError("Falha no login: ${e.message}")
                }
            }
        }
    }

    fun primeiroAcesso(
        cpf: String,
        senha: String,
        confirmarSenha: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp = api.primeiroAcesso(
                    PrimeiroAcessoRequest(
                        cpf = cpf,
                        senha = senha,
                        confirmarSenha = confirmarSenha
                    )
                )

                if (resp.ok) {
                    CoroutineScope(Dispatchers.Main).launch {
                        onSuccess("${resp.message}\nLogin: ${resp.loginMobile ?: cpf}")
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch { onError(resp.message) }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onError("Falha ao criar acesso: ${e.message}")
                }
            }
        }
    }

    fun baterPonto(
        login: String,
        senha: String,
        latitude: Double?,
        longitude: Double?,
        precisaoGps: Double?,
        fotoBase64: String?,
        observacao: String,
        deviceInfo: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (fotoBase64.isNullOrBlank()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        onError("Capture a selfie antes de bater o ponto.")
                    }
                    return@launch
                }

                val resp = api.baterPonto(
                    BaterPontoRequest(
                        login = login,
                        senha = senha,
                        latitude = latitude,
                        longitude = longitude,
                        precisaoGps = precisaoGps,
                        fotoBase64 = fotoBase64,
                        observacao = observacao,
                        deviceInfo = deviceInfo
                    )
                )

                if (resp.ok) {
                    CoroutineScope(Dispatchers.Main).launch { onSuccess(resp.message) }
                } else {
                    CoroutineScope(Dispatchers.Main).launch { onError(resp.message) }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onError("Falha ao bater ponto: ${e.message}")
                }
            }
        }
    }

    fun historico(
        login: String,
        senha: String,
        onSuccess: (List<HistoricoItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp = api.historico(
                    HistoricoRequest(
                        login = login,
                        senha = senha,
                        limite = 20
                    )
                )

                if (resp.ok) {
                    CoroutineScope(Dispatchers.Main).launch { onSuccess(resp.historico) }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        onError(resp.message ?: "Erro ao carregar histórico.")
                    }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onError("Falha ao carregar histórico: ${e.message}")
                }
            }
        }
    }
}
