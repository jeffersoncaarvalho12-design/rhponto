package com.technet.rhponto

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.google.android.gms.location.LocationServices
import com.technet.rhponto.data.MobileRepository
import com.technet.rhponto.model.AppUser
import com.technet.rhponto.ui.HomeScreen
import com.technet.rhponto.ui.LoginScreen
import com.technet.rhponto.ui.PrimeiroAcessoScreen
import com.technet.rhponto.ui.theme.AppTheme
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {

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
                    AppNav(repository)
                }
            }
        }
    }
}

@Composable
private fun AppNav(repository: MobileRepository) {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<AppUser?>(null) }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onPrimeiroAcesso = {
                    navController.navigate("primeiro_acesso")
                },
                onLogin = { login, senha, onResult ->
                    repository.login(
                        login = login,
                        senha = senha,
                        onSuccess = { user ->
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

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { }

            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicturePreview()
            ) { bitmap: Bitmap? ->
                if (bitmap != null) {
                    repository.pendingBitmap = bitmap
                }
            }

            HomeScreen(
                user = user,
                onSolicitarPermissoes = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA
                        )
                    )
                },
                onCapturarSelfie = {
                    cameraLauncher.launch(null)
                },
                onBaterPonto = { observacao, onResult ->
                    val fotoBase64 = repository.pendingBitmap?.toBase64Jpeg()

                    if (fotoBase64.isNullOrBlank()) {
                        onResult(null, "Capture a selfie antes de bater o ponto.")
                        return@HomeScreen
                    }

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
                                    observacao = observacao,
                                    deviceInfo = "Android App RH Ponto",
                                    onSuccess = { msg ->
                                        onResult(msg, null)
                                    },
                                    onError = { err ->
                                        onResult(null, err)
                                    }
                                )
                            }
                            .addOnFailureListener { e ->
                                onResult(null, "Falha ao obter localização: ${e.message}")
                            }
                    } catch (e: SecurityException) {
                        onResult(null, "Permissão de localização não concedida.")
                    }
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
                    repository.pendingBitmap = null
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun Bitmap.toBase64Jpeg(): String {
    val output = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 85, output)
    return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
}
