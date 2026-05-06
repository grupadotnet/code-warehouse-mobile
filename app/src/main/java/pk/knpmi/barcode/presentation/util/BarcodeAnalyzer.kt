package pk.knpmi.barcode.presentation.util

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

// It is used to analyze frame per interval and trying to find any barcode 1D or 2D
class BarcodeAnalyzer(
    private val dedupeWindowMs: Long = 1200L, // Dedupe same barcode to avoid spamming UI while camera is steady
    private val onBarcodeDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var lastValue: String? = null
    private var lastAtMs: Long = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close() // Always close frames, otherwise CameraX analysis will stall
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees) // ML Kit needs rotation to interpret the image correctly

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes
                    .firstOrNull()
                    ?.rawValue
                    ?.takeIf { it.isNotBlank() }
                    ?: barcodes.firstOrNull()?.displayValue?.takeIf { it.isNotBlank() }

                // Emit only if value is new or enough time passed
                if (value != null) {
                    val now = System.currentTimeMillis()
                    val shouldEmit = value != lastValue || (now - lastAtMs) >= dedupeWindowMs
                    if (shouldEmit) {
                        lastValue = value
                        lastAtMs = now
                        onBarcodeDetected(value) // Callback
                    }
                }
            }
            .addOnFailureListener {
                // Intentionally ignored, analyzer continues with next frames
            }
            .addOnCompleteListener {
                imageProxy.close() // Always close frames, otherwise CameraX analysis will stall
            }
    }
}