package pk.knpmi.barcode.presentation.util

import kotlinx.serialization.Serializable

sealed class Screen {

    @Serializable
    data class CameraScreen(
        val mode: ScanMode = ScanMode.PRODUCT,
        val productBarcode: String? = null,
    )

    @Serializable
    data class Test(
        val barcode: String? = null,
        val scannedLocalisationId: String? = null,
    )
}

@Serializable
enum class ScanMode {
    PRODUCT,
    LOCATION,
}