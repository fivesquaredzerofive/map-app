package com.interactivemaps.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.interactivemaps.app.ui.components.DrawerContent
import com.interactivemaps.app.ui.screens.MapScreen
import com.interactivemaps.app.ui.screens.SettingsScreen
import com.interactivemaps.app.ui.theme.InteractiveMapsAppTheme
import com.interactivemaps.app.ui.viewmodel.MapViewModel
import com.interactivemaps.app.ui.viewmodel.MapViewModelFactory
import com.interactivemaps.app.ui.viewmodel.SettingsViewModel
import com.interactivemaps.app.ui.viewmodel.SettingsViewModelFactory
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

        setContent {
            InteractiveMapsAppTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                
                NavHost(navController = navController, startDestination = "map") {
                    composable("map") {
                        val markers by mapViewModel.markers.collectAsState()
                        
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                DrawerContent(
                                    markers = markers,
                                    onMarkerClick = { marker ->
                                        // Optional: trigger map camera pan
                                        scope.launch { drawerState.close() }
                                    },
                                    onMarkerDelete = { marker ->
                                        mapViewModel.deleteMarker(marker)
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
