package com.technet.rhponto.data

import android.content.Context
import android.graphics.Bitmap
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.technet.rhponto.model.AppUser
import com.technet.rhponto.model.HistoricoItem
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

    var pendingBitmap: Bitmap? = null

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
                val resp = api.login(
                    mapOf(
                        "login" to login,
                        "senha" to senha
                    )
                )

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
                CoroutineScope(Dispatchers.Main).launch { onError("Falha no login: ${e.message}") }
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
                val resp = api.baterPonto(
                    mapOf(
                        "login" to login,
                        "senha" to senha,
                        "latitude" to latitude,
                        "longitude" to longitude,
                        "precisao_gps" to precisaoGps,
                        "foto_base64" to fotoBase64,
                        "observacao" to observacao,
                        "device_info" to deviceInfo
                    )
                )

                if (resp.ok) {
                    CoroutineScope(Dispatchers.Main).launch { onSuccess(resp.message) }
                } else {
                    CoroutineScope(Dispatchers.Main).launch { onError(resp.message) }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch { onError("Falha ao bater ponto: ${e.message}") }
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
                    mapOf(
                        "login" to login,
                        "senha" to senha,
                        "limite" to 20
                    )
                )

                if (resp.ok) {
                    CoroutineScope(Dispatchers.Main).launch { onSuccess(resp.historico) }
                } else {
                    CoroutineScope(Dispatchers.Main).launch { onError(resp.message ?: "Erro ao carregar histórico.") }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch { onError("Falha ao carregar histórico: ${e.message}") }
            }
        }
    }
}
