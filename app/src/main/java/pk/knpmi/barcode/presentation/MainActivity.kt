package pk.knpmi.barcode.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import pk.knpmi.barcode.presentation.camera_screen.CameraScannerScreen
import pk.knpmi.barcode.presentation.test_screen.TestScreen
import pk.knpmi.barcode.presentation.ui.theme.ProjectBarcodeTheme
import pk.knpmi.barcode.presentation.util.ScanMode
import pk.knpmi.barcode.presentation.util.Screen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectBarcodeTheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.CameraScreen()
                    ){
                        composable<Screen.CameraScreen> { backStackEntry ->
                            val route: Screen.CameraScreen = backStackEntry.toRoute()
                            CameraScannerScreen { barcode ->
                                when (route.mode) {
                                    ScanMode.PRODUCT -> {
                                        // First scan: product -> go to TestScreen with product barcode
                                        navController.navigate(Screen.Test(barcode = barcode)) {
                                            popUpTo(Screen.CameraScreen()) { inclusive = true }
                                        }
                                    }
                                    ScanMode.LOCATION -> {
                                        // Second scan: location -> back to TestScreen with scanned localisation
                                        val productBarcode = route.productBarcode ?: ""
                                        navController.navigate(
                                            Screen.Test(
                                                barcode = productBarcode,
                                                scannedLocalisationId = barcode,
                                            ),
                                        ) {
                                            popUpTo(Screen.Test(barcode = productBarcode)) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }

                        composable<Screen.Test> { backStackEntry ->
                            val route: Screen.Test = backStackEntry.toRoute()

                            TestScreen(
                                barcode = route.barcode ?: "",
                                scannedLocalisationId = route.scannedLocalisationId,
                                onScanProduct = {
                                    navController.navigate(Screen.CameraScreen(mode = ScanMode.PRODUCT))
                                },
                                onScanLocalisation = {
                                    navController.navigate(
                                        Screen.CameraScreen(
                                            mode = ScanMode.LOCATION,
                                            productBarcode = route.barcode ?: "",
                                        ),
                                    )
                                },
                                onSaved = {
                                    navController.navigate(Screen.CameraScreen(mode = ScanMode.PRODUCT)) {
                                        popUpTo(Screen.Test(barcode = route.barcode ?: "")) { inclusive = true }
                                    }
                                },
                            )
                        }
                    }

                }
            }
        }
    }
}