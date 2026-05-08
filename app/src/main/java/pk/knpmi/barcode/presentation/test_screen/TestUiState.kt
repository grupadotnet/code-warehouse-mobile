package pk.knpmi.barcode.presentation.test_screen

import pk.knpmi.barcode.domain.model.Localisation
import pk.knpmi.barcode.domain.model.Product
import pk.knpmi.barcode.domain.model.ProductMetadata

data class TestUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,

    val barcodeInput: String = "",

    val metadata: ProductMetadata? = null,

    val product: Product? = null,
    val pendingNewProductBarcode: String? = null,
    val awaitingMoveTargetLocalisationScan: Boolean = false,

    val localisation: Localisation? = null,
    val productsAtLocalisation: List<Product> = emptyList(),

    val name: String = "",
    val category: String = "",
    val quantityText: String = "1",
    val localisationId: String = "",
)

