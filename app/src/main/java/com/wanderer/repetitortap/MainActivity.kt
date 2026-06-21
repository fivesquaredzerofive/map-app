package com.wanderer.repetitortap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wanderer.repetitortap.data.MarkerEntity
import com.wanderer.repetitortap.ui.components.DrawerContent
import com.wanderer.repetitortap.ui.screens.LoginScreen
import com.wanderer.repetitortap.ui.screens.MapScreen
import com.wanderer.repetitortap.ui.screens.SettingsScreen
import com.wanderer.repetitortap.ui.screens.SignupScreen
import com.wanderer.repetitortap.ui.theme.InteractiveMapsAppTheme
import com.wanderer.repetitortap.ui.viewmodel.AuthViewModel
import com.wanderer.repetitortap.ui.viewmodel.AuthViewModelFactory
import com.wanderer.repetitortap.ui.viewmodel.MapViewModel
import com.wanderer.repetitortap.ui.viewmodel.MapViewModelFactory
import com.wanderer.repetitortap.ui.viewmodel.SettingsViewModel
import com.wanderer.repetitortap.ui.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MapLibre BEFORE content is set
        MapLibre.getInstance(this)

        enableEdgeToEdge()

        val app = application as MapApplication
        val mapViewModel = ViewModelProvider(
            this,
            MapViewModelFactory(app.database.markerDao(), app.settingsDataStore)
        )[MapViewModel::class.java]

        val settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(app.settingsDataStore)
        )[SettingsViewModel::class.java]

        val authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(app.authRepository)
        )[AuthViewModel::class.java]

        setContent {
            InteractiveMapsAppTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var focusedMarker by remember { mutableStateOf<MarkerEntity?>(null) }
                var markerFocusRequestId by remember { mutableStateOf(0) }

                val authUser by authViewModel.authState.collectAsState()

                // Determine start destination based on auth state
                val startDestination = if (authUser != null) "map" else "login"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        // If already logged in, redirect to map
                        if (authUser != null) {
                            LaunchedEffect(Unit) {
                                navController.navigate("map") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }

                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToSignup = { navController.navigate("signup") }
                        )
                    }

                    composable("signup") {
                        SignupScreen(
                            viewModel = authViewModel,
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("map") {
                        // Auth guard: if not logged in, redirect to login
                        if (authUser == null) {
                            LaunchedEffect(Unit) {
                                navController.navigate("login") {
                                    popUpTo("map") { inclusive = true }
                                }
                            }
                        }

                        val markers by mapViewModel.markers.collectAsState()

                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                DrawerContent(
                                    markers = markers,
                                    onMarkerClick = { marker ->
                                        focusedMarker = marker
                                        markerFocusRequestId += 1
                                        scope.launch { drawerState.close() }
                                    },
                                    onMarkerDelete = { marker ->
                                        mapViewModel.deleteMarker(marker)
                                    },
                                    onMarkerRename = { marker, title ->
                                        mapViewModel.updateMarkerTitle(marker, title)
                                    },
                                    onMarkerColorChange = { marker, color ->
                                        mapViewModel.updateMarkerColor(marker, color)
                                    },
                                    onSettingsClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("settings")
                                    }
                                )
                            }
                        ) {
                            MapScreen(
                                viewModel = mapViewModel,
                                focusedMarker = focusedMarker,
                                markerFocusRequestId = markerFocusRequestId,
                                onMenuClick = {
                                    scope.launch { drawerState.open() }
                                }
                            )
                        }
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
