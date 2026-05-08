package pk.knpmi.barcode.presentation.util

import kotlinx.serialization.Serializable

sealed class Screen {

    @Serializable
    object CameraScreen

    @Serializable
    data class Test(val barcode: String? = null)
}