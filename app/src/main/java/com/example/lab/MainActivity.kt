@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.lab.data.Campings
import com.example.lab.network.RetrofitClient
import com.example.lab.network.toCampings
import kotlinx.coroutines.launch
import com.example.lab.navigation.AppNavGraph
import com.example.lab.ui.theme.LABTheme
import com.example.lab.ui.screens.LoadingScreen
import com.example.lab.ui.screens.ErrorScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Mantiene el splash visible mientras cargamos datos
        var isLoading = true
        splashScreen.setKeepOnScreenCondition { isLoading }

        setContent {
            LABTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scope = rememberCoroutineScope()

                    var campings by remember { mutableStateOf<List<Campings>>(emptyList()) }
                    var error    by remember { mutableStateOf<String?>(null) }
                    var loading  by remember { mutableStateOf(true) }

                    fun loadData() {
                        loading = true
                        error   = null
                        scope.launch {
                            try {
                                val response = RetrofitClient.service.getCampings()
                                campings = response.result.records.map { it.toCampings() }
                            } catch (e: Exception) {
                                error = "Error al cargar los campings:\n${e.message}"
                            } finally {
                                loading   = false
                                isLoading = false  // cierra el splash
                            }
                        }
                    }

                    LaunchedEffect(Unit) { loadData() }

                    when {
                        loading       -> LoadingScreen()
                        error != null -> ErrorScreen(message = error!!, onRetry = ::loadData)
                        else          -> AppNavGraph(campings)
                    }
                }
            }
        }
    }
}
