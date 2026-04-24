package com.technet.rhponto.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val ok: Boolean,
    val message: String,
    val funcionario: FuncionarioPayload? = null
)

@Serializable
data class FuncionarioPayload(
    val id: Int,
    val nome: String,
    val matricula: String,
    val status: String,
    @SerialName("login_mobile")
    val loginMobile: String
)

data class AppUser(
    val id: Int,
    val nome: String,
    val matricula: String,
    val status: String,
    val loginMobile: String,
    val senha: String
)

@Serializable
data class PrimeiroAcessoResponse(
    val ok: Boolean,
    val message: String,
    @SerialName("login_mobile")
    val loginMobile: String? = null
)

@Serializable
data class BatidaResponse(
    val ok: Boolean,
    val message: String
)

@Serializable
data class HistoricoResponse(
    val ok: Boolean,
    val historico: List<HistoricoItem> = emptyList(),
    val message: String? = null
)

@Serializable
data class HistoricoItem(
    val id: Int,
    @SerialName("data_hora")
    val dataHora: String,
    @SerialName("tipo_marcacao")
    val tipoMarcacao: String
)
