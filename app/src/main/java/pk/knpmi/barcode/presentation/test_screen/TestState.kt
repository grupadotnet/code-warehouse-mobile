package pk.knpmi.barcode.presentation.test_screen

import pk.knpmi.barcode.domain.model.Localisation
import pk.knpmi.barcode.domain.model.Product
import pk.knpmi.barcode.domain.model.ProductMetadata

data class TestState(
    val barcodeInput: String = "",
    val product: Product? = null,
    val localisation: Localisation? = null,
    val productsAtLocalisation: List<Product> = emptyList(),
    val metadata: ProductMetadata? = null,
    val pendingNewProductBarcode: String? = null,
    val awaitingMoveTargetLocalisationScan: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null,
)
