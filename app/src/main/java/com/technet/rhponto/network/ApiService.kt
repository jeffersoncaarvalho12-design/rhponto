package com.technet.rhponto.network

import com.technet.rhponto.model.BaterPontoRequest
import com.technet.rhponto.model.BatidaResponse
import com.technet.rhponto.model.HistoricoRequest
import com.technet.rhponto.model.HistoricoResponse
import com.technet.rhponto.model.LoginRequest
import com.technet.rhponto.model.LoginResponse
import com.technet.rhponto.model.PrimeiroAcessoRequest
import com.technet.rhponto.model.PrimeiroAcessoResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login.php")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("primeiro_acesso.php")
    suspend fun primeiroAcesso(@Body body: PrimeiroAcessoRequest): PrimeiroAcessoResponse

    @POST("bater_ponto.php")
    suspend fun baterPonto(@Body body: BaterPontoRequest): BatidaResponse

    @POST("meu_historico.php")
    suspend fun historico(@Body body: HistoricoRequest): HistoricoResponse
}
