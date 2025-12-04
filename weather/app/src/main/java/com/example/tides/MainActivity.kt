package com.example.tides

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tides.screens.landingScreen.LandingScreen
import com.example.tides.screens.noLocationScreen.NoLocationScreen
import com.example.tides.ui.theme.TidesTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permission =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        setContent {
            val darkMode by viewModel.darkMode.collectAsState()

            var hasLocationPermission by remember { mutableStateOf(if (permission) true else null) }
            val context = LocalContext.current

            val permissionLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { permissions ->
                        hasLocationPermission =
                            permissions.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                false
                            ) ||
                                    permissions.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        false,
                                    )
                    },
                )

            LaunchedEffect(Unit) {
                val isGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED

                if (isGranted) {
                    hasLocationPermission = true
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            }

            TidesTheme(darkTheme = darkMode ?: isSystemInDarkTheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (hasLocationPermission) {
                        true ->
                            AppNavHost(
                                startDestination = "landing_screen",
                                viewModel = viewModel,
                            )

                        false ->
                            AppNavHost(
                                startDestination = "no_location_screen",
                                viewModel = viewModel,
                            )

                        null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    startDestination: String,
    viewModel: MainActivityViewModel,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("landing_screen") {
            val darkMode by viewModel.darkMode.collectAsState()
            Scaffold { innerPadding ->
                Column(
                    modifier =
                        Modifier.padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding(),
                        ),
                ) {
                    LandingScreen(
                        darkMode = darkMode ?: isSystemInDarkTheme(),
                        onThemeChange = { viewModel.saveDarkMode(it) },
                    )
                }
            }
        }

        composable("no_location_screen") {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                NoLocationScreen(
                    onPermissionGranted = {
                        navController.navigate("landing_screen") {
                            popUpTo("no_location_screen") { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
