package com.technet.rhponto

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.*
import com.google.android.gms.location.LocationServices
import com.technet.rhponto.data.MobileRepository
import com.technet.rhponto.data.SecureCredentialStore
import com.technet.rhponto.model.AppUser
import com.technet.rhponto.ui.HomeScreen
import com.technet.rhponto.ui.LoginScreen
import com.technet.rhponto.ui.PrimeiroAcessoScreen
import com.technet.rhponto.ui.theme.AppTheme
import java.io.ByteArrayOutputStream

class MainActivity : FragmentActivity() {

    private val repository by lazy { MobileRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(this, repository)
                }
            }
        }
    }
}

@Composable
private fun AppNav(
    activity: FragmentActivity,
    repository: MobileRepository
) {
    val navController = rememberNavController()
    val credentialStore = remember { SecureCredentialStore(repository.context) }

    var currentUser by remember { mutableStateOf<AppUser?>(null) }
    var savedLogin by remember { mutableStateOf(credentialStore.getLogin()) }
    var hasSavedCredentials by remember { mutableStateOf(credentialStore.hasCredentials()) }

    var pendingObservacao by remember { mutableStateOf("") }
    var pendingCallback by remember {
        mutableStateOf<((String?, String?) -> Unit)?>(null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        val callback = pendingCallback
        val user = currentUser

        if (bitmap == null || callback == null || user == null) {
            callback?.invoke(null, "Selfie não capturada.")
            return@rememberLauncherForActivityResult
        }

        val fotoBase64 = bitmap.toBase64Jpeg()
        val fused = LocationServices.getFusedLocationProviderClient(repository.context)

        try {
            fused.lastLocation
                .addOnSuccessListener { location ->
                    repository.baterPonto(
                        login = user.loginMobile,
                        senha = user.senha,
                        latitude = location?.latitude,
                        longitude = location?.longitude,
                        precisaoGps = location?.accuracy?.toDouble(),
                        fotoBase64 = fotoBase64,
                        observacao = pendingObservacao,
                        deviceInfo = "Android App RH Ponto",
                        onSuccess = { msg ->
                            callback.invoke(msg, null)
                        },
                        onError = { err ->
                            callback.invoke(null, err)
                        }
                    )
                }
                .addOnFailureListener { e ->
                    callback.invoke(null, "Falha ao obter localização: ${e.message}")
                }
        } catch (e: SecurityException) {
            callback.invoke(null, "Permissão de localização não concedida.")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        cameraLauncher.launch(null)
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                savedLogin = savedLogin,
                hasSavedCredentials = hasSavedCredentials,
                onPrimeiroAcesso = {
                    navController.navigate("primeiro_acesso")
                },
                onBiometricLogin = { onResult ->
                    if (!credentialStore.hasCredentials()) {
                        onResult("Nenhum acesso salvo neste aparelho.")
                        return@LoginScreen
                    }

                    authenticateWithBiometric(
                        activity = activity,
                        onSuccess = {
                            val loginSalvo = credentialStore.getLogin()
                            val senhaSalva = credentialStore.getSenha()

                            repository.login(
                                login = loginSalvo,
                                senha = senhaSalva,
                                onSuccess = { user ->
                                    currentUser = user
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onError = { error ->
                                    onResult(error)
                                }
                            )
                        },
                        onError = { error ->
                            onResult(error)
                        }
                    )
                },
                onLogin = { login, senha, salvarAcesso, onResult ->
                    repository.login(
                        login = login,
                        senha = senha,
                        onSuccess = { user ->
                            if (salvarAcesso) {
                                credentialStore.save(login, senha)
                                savedLogin = login
                                hasSavedCredentials = true
                            }

                            currentUser = user
                            onResult(null)
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onError = { error ->
                            onResult(error)
                        }
                    )
                }
            )
        }

        composable("primeiro_acesso") {
            PrimeiroAcessoScreen(
                onVoltar = {
                    navController.popBackStack()
                },
                onCriarAcesso = { cpf, senha, confirmarSenha, onResult ->
                    repository.primeiroAcesso(
                        cpf = cpf,
                        senha = senha,
                        confirmarSenha = confirmarSenha,
                        onSuccess = { msg ->
                            credentialStore.save(cpf, senha)
                            savedLogin = cpf
                            hasSavedCredentials = true
                            onResult(msg, null)
                        },
                        onError = { err ->
                            onResult(null, err)
                        }
                    )
                }
            )
        }

        composable("home") {
            val user = currentUser

            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
                return@composable
            }

            HomeScreen(
                user = user,
                onBaterPonto = { observacao, onResult ->
                    pendingObservacao = observacao
                    pendingCallback = onResult

                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA
                        )
                    )
                },
                onHistorico = { onResult ->
                    repository.historico(
                        login = user.loginMobile,
                        senha = user.senha,
                        onSuccess = { itens ->
                            onResult(
                                itens.joinToString("\n") {
                                    "${it.dataHora} - ${it.tipoMarcacao}"
                                },
                                null
                            )
                        },
                        onError = { err ->
                            onResult(null, err)
                        }
                    )
                },
                onLogout = {
                    currentUser = null
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun authenticateWithBiometric(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val biometricManager = BiometricManager.from(activity)

    val canAuthenticate = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )

    if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
        onError("Biometria não disponível ou não configurada neste celular.")
        return
    }

    val executor = ContextCompat.getMainExecutor(activity)

    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Biometria não reconhecida.")
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Entrar no RH Ponto")
        .setSubtitle("Use a biometria ou senha do celular")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    prompt.authenticate(promptInfo)
}

private fun Bitmap.toBase64Jpeg(): String {
    val output = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 85, output)
    return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
}
