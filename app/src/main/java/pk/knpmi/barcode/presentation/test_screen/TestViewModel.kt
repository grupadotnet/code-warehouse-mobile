package pk.knpmi.barcode.presentation.test_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pk.knpmi.barcode.domain.model.Localisation
import pk.knpmi.barcode.domain.model.Product
import pk.knpmi.barcode.domain.repository.LocalisationRepository
import pk.knpmi.barcode.domain.repository.ProductRepository
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val localisationRepository: LocalisationRepository,
) : ViewModel() {

    private val _state = mutableStateOf(TestState())
    val state: State<TestState> = _state

    init {
        refreshMetadata()
    }

    fun onBarcodeInputChange(value: String) {
        _state.value = _state.value.copy(barcodeInput = value, message = null)
    }

    fun scanProduct(barcode: String = state.value.barcodeInput.trim()) {
        if (barcode.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null)
            val product = productRepository.getByBarcode(barcode)

            _state.value =
                if (product != null) {
                    _state.value.copy(
                        isLoading = false,
                        product = product,
                        pendingNewProductBarcode = null,
                        awaitingMoveTargetLocalisationScan = false,
                        message = "Znaleziono produkt: ${product.id}",
                    )
                } else {
                    _state.value.copy(
                        isLoading = false,
                        product = null,
                        pendingNewProductBarcode = barcode,
                        awaitingMoveTargetLocalisationScan = false,
                        message = "Brak produktu. Możesz utworzyć nowy dla kodu: $barcode",
                    )
                }
        }
    }

    fun scanLocalisation(barcode: String = state.value.barcodeInput.trim()) {
        if (barcode.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null)

            val current = _state.value
            val location = findLocalisationOrFallback(barcode)

            if (current.awaitingMoveTargetLocalisationScan && current.product != null) {
                val moved =
                    current.product.copy(
                        localisationId = barcode,
                        date = System.currentTimeMillis(),
                    )
                productRepository.update(moved)
                val productsAt = localisationRepository.getProducts(barcode)

                _state.value =
                    current.copy(
                        isLoading = false,
                        product = moved,
                        localisation = location,
                        productsAtLocalisation = productsAt,
                        awaitingMoveTargetLocalisationScan = false,
                        message = "Przeniesiono ${moved.id} do lokalizacji: $barcode",
                    )
                return@launch
            }

            val productsAt = localisationRepository.getProducts(barcode)
            _state.value =
                _state.value.copy(
                    isLoading = false,
                    localisation = location,
                    productsAtLocalisation = productsAt,
                    awaitingMoveTargetLocalisationScan = false,
                    message = "Produkty w lokalizacji: $barcode (${productsAt.size})",
                )
        }
    }

    fun beginMoveSelectedProduct() {
        val product = _state.value.product ?: return
        _state.value =
            _state.value.copy(
                awaitingMoveTargetLocalisationScan = true,
                message = "Zeskanuj lokalizację docelową dla produktu: ${product.id}",
            )
    }

    fun createOrOverwriteProduct(
        name: String,
        category: String,
        localisationId: String,
        quantity: Double,
    ) {
        val barcode = _state.value.pendingNewProductBarcode ?: _state.value.product?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null)
            val product =
                Product(
                    id = barcode,
                    name = name,
                    category = category,
                    localisationId = localisationId,
                    quantity = quantity,
                    date = System.currentTimeMillis(),
                )
            val saved =
                if (_state.value.product == null) {
                    productRepository.create(product)
                } else {
                    productRepository.update(product)
                }

            _state.value =
                _state.value.copy(
                    isLoading = false,
                    product = saved,
                    pendingNewProductBarcode = null,
                    message = "Zapisano produkt: ${saved.id}",
                )
        }
    }

    fun deleteSelectedProduct() {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null)
            productRepository.delete(product.id)
            _state.value =
                _state.value.copy(
                    isLoading = false,
                    product = null,
                    awaitingMoveTargetLocalisationScan = false,
                    message = "Usunięto produkt: ${product.id}",
                )
        }
    }

    private fun refreshMetadata() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null)
            val metadata = productRepository.getMetadata()
            _state.value = _state.value.copy(isLoading = false, metadata = metadata)
        }
    }

    private suspend fun findLocalisationOrFallback(id: String): Localisation {
        val all = localisationRepository.getAll()
        return all.firstOrNull { it.id == id } ?: Localisation(id = id, name = id)
    }
}
