package com.technet.rhponto.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureCredentialStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "rh_ponto_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(login: String, senha: String) {
        prefs.edit()
            .putString("login", login)
            .putString("senha", senha)
            .apply()
    }

    fun getLogin(): String = prefs.getString("login", "") ?: ""

    fun getSenha(): String = prefs.getString("senha", "") ?: ""

    fun hasCredentials(): Boolean {
        return getLogin().isNotBlank() && getSenha().isNotBlank()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
