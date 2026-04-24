package com.technet.rhponto.network

import com.technet.rhponto.model.BatidaResponse
import com.technet.rhponto.model.HistoricoResponse
import com.technet.rhponto.model.LoginResponse
import com.technet.rhponto.model.PrimeiroAcessoResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login.php")
    suspend fun login(@Body body: Map<String, String>): LoginResponse

    @POST("primeiro_acesso.php")
    suspend fun primeiroAcesso(@Body body: Map<String, String>): PrimeiroAcessoResponse

    @POST("bater_ponto.php")
    suspend fun baterPonto(@Body body: Map<String, @JvmSuppressWildcards Any?>): BatidaResponse

    @POST("meu_historico.php")
    suspend fun historico(@Body body: Map<String, @JvmSuppressWildcards Any?>): HistoricoResponse
}
