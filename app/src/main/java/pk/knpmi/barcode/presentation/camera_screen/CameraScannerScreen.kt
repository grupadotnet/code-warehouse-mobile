package pk.knpmi.barcode.presentation.camera_screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import pk.knpmi.barcode.presentation.util.BarcodeAnalyzer
import pk.knpmi.barcode.presentation.util.rememberCameraPermissionState
import java.util.concurrent.Executors

@Composable
fun CameraScannerScreen(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Use the separated permission logic
    val permissionState = rememberCameraPermissionState()

    // Ask on first composition, user can retry via UI below.
    LaunchedEffect(Unit) {
        if (!permissionState.hasPermission) {
            permissionState.onRequestPermission()
        }
    }

    // Analyzer must run off the UI thread
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    if (!permissionState.hasPermission) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No camera permission")
            Button(
                onClick = {
                    // Open settings to allow user to grant permission manually
                    permissionState.onOpenSettings()
                }
            ) {
                Text("Give permission")
            }
        }
        return
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            // Bind use-cases on the main thread (lifecycle + View)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()

                    // Connect the CameraX Preview use-case to the PreviewView so the camera frames are rendered on screen
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Drop old frames, keep scan realtime
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                cameraExecutor,
                                BarcodeAnalyzer { value ->
                                    // Analyzer runs on a background thread, UI calls (Toast) must be posted to the main thread to avoid threading issues.
                                    ContextCompat.getMainExecutor(ctx).execute {
                                        onBarcodeScanned(value) // Launch form screen
                                    }
                                },
                            )
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // Prevent duplicate bindings when recreated
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis,
                    )
                },
                ContextCompat.getMainExecutor(ctx),
            )

            previewView
        },
    )
}

