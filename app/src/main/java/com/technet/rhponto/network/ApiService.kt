package com.technet.rhponto.network

import com.technet.rhponto.model.BatidaResponse
import com.technet.rhponto.model.HistoricoResponse
import com.technet.rhponto.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login.php")
    suspend fun login(@Body body: Map<String, String>): LoginResponse

    @POST("bater_ponto.php")
    suspend fun baterPonto(@Body body: Map<String, @JvmSuppressWildcards Any?>): BatidaResponse

    @POST("meu_historico.php")
    suspend fun historico(@Body body: Map<String, @JvmSuppressWildcards Any?>): HistoricoResponse
}
