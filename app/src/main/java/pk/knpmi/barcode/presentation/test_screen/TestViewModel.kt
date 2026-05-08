package pk.knpmi.barcode.presentation.test_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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

    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState

    private val _events = MutableSharedFlow<TestEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<TestEvent> = _events.asSharedFlow()

    fun load(initialProductBarcode: String, scannedLocalisationId: String?) {
        if (_uiState.value.metadata == null) {
            refreshMetadata()
        }

        if (initialProductBarcode.isNotBlank() && _uiState.value.barcodeInput.isBlank()) {
            onBarcodeInputChange(initialProductBarcode)
            scanProduct()
        }

        if (!scannedLocalisationId.isNullOrBlank()) {
            onBarcodeInputChange(scannedLocalisationId)
            scanLocalisation()
        }
    }

    fun onBarcodeInputChange(value: String) =
        _uiState.update { it.copy(barcodeInput = value, message = null) }

    private fun refreshMetadata() {
        viewModelScope.launch {
            try {
                val metadata = productRepository.getMetadata()
                _uiState.update { prev ->
                    prev.copy(
                        metadata = metadata,
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(message = t.message ?: "Metadata error") }
            }
        }
    }

    fun scanProduct() {
        val barcode = _uiState.value.barcodeInput.trim()
        if (barcode.isBlank()) return

        _uiState.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            try {
                val metadataDeferred = async { _uiState.value.metadata ?: productRepository.getMetadata() }
                val productDeferred = async { productRepository.getByBarcode(barcode) }

                val metadata = metadataDeferred.await()
                val product = productDeferred.await()

                _uiState.update { prev ->
                    val defaultCategory = metadata.categories.firstOrNull().orEmpty()
                    prev.copy(
                        isLoading = false,
                        metadata = metadata,
                        product = product,
                        pendingNewProductBarcode = if (product == null) barcode else null,
                        awaitingMoveTargetLocalisationScan = false,
                        name = product?.name ?: "",
                        category = product?.category ?: prev.category.ifBlank { defaultCategory },
                        localisationId = product?.localisationId ?: prev.localisation?.id.orEmpty(),
                        quantityText = product?.quantity?.toString() ?: "1",
                        message = if (product == null) "Nowy produkt: wypełnij formularz i kliknij SAVE." else null,
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, message = t.message ?: "Scan product error") }
            }
        }
    }

    fun scanLocalisation() {
        val localisationId = _uiState.value.barcodeInput.trim()
        if (localisationId.isBlank()) return

        _uiState.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            try {
                val localisationDeferred = async { findLocalisationOrFallback(localisationId) }
                val productsDeferred = async { localisationRepository.getProducts(localisationId) }

                val localisation = localisationDeferred.await()
                val products = productsDeferred.await()

                val awaitingMove = _uiState.value.awaitingMoveTargetLocalisationScan
                val selected = _uiState.value.product

                if (awaitingMove && selected != null) {
                    val moved = selected.copy(localisationId = localisationId, date = System.currentTimeMillis())
                    productRepository.update(moved)
                    _uiState.update {
                        it.copy(
                            product = moved,
                            awaitingMoveTargetLocalisationScan = false,
                            localisationId = localisationId,
                            message = "Przeniesiono ${selected.id} -> $localisationId",
                        )
                    }
                }

                _uiState.update { prev ->
                    prev.copy(
                        isLoading = false,
                        localisation = localisation,
                        productsAtLocalisation = products,
                        localisationId = localisationId,
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, message = t.message ?: "Scan location error") }
            }
        }
    }

    fun beginMoveSelectedProduct() {
        val selected = _uiState.value.product ?: return
        _uiState.update {
            it.copy(
                awaitingMoveTargetLocalisationScan = true,
                message = "Tryb MOVE: zeskanuj lokalizację docelową (Scan LOCATION).",
                localisationId = selected.localisationId,
            )
        }
    }

    fun deleteSelectedProduct() {
        val selected = _uiState.value.product ?: return
        _uiState.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            try {
                productRepository.delete(selected.id)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        product = null,
                        pendingNewProductBarcode = null,
                        name = "",
                        quantityText = "1",
                        message = "Usunięto ${selected.id}",
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, message = t.message ?: "Delete error") }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }
    fun onQuantityChange(value: String) = _uiState.update { it.copy(quantityText = value) }
    fun onLocalisationChange(value: String) = _uiState.update { it.copy(localisationId = value) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val name = state.name.trim()
        val category = state.category.trim()
        val localisationId = state.localisationId.trim()
        val qty = state.quantityText.replace(',', '.').toDoubleOrNull()

        if (name.isBlank()) {
            _uiState.update { it.copy(message = "Nazwa nie może być pusta") }
            return
        }
        if (category.isBlank()) {
            _uiState.update { it.copy(message = "Kategoria nie może być pusta") }
            return
        }
        if (localisationId.isBlank()) {
            _uiState.update { it.copy(message = "Lokalizacja nie może być pusta") }
            return
        }
        if (qty == null || qty <= 0.0) {
            _uiState.update { it.copy(message = "Ilość musi być liczbą dodatnią (> 0)") }
            return
        }

        _uiState.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            try {
                val existing = state.product
                val barcode = existing?.id ?: state.pendingNewProductBarcode ?: state.barcodeInput.trim()
                if (barcode.isBlank()) {
                    _uiState.update { it.copy(isSaving = false, message = "Brak barcode produktu") }
                    return@launch
                }

                val product =
                    if (existing != null) {
                        existing.copy(
                            name = name.ifBlank { existing.name },
                            category = category.ifBlank { existing.category },
                            localisationId = localisationId.ifBlank { existing.localisationId },
                            quantity = qty,
                            date = System.currentTimeMillis(),
                        )
                    } else {
                        Product(
                            id = barcode,
                            name = name,
                            category = category,
                            localisationId = localisationId,
                            quantity = qty,
                            date = System.currentTimeMillis(),
                        )
                    }

                val saved =
                    if (existing == null) productRepository.create(product) else productRepository.update(product)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        product = saved,
                        pendingNewProductBarcode = null,
                    )
                }
                _events.tryEmit(TestEvent.Saved)
            } catch (t: Throwable) {
                _uiState.update { it.copy(isSaving = false, message = t.message ?: "Unknown error") }
            }
        }
    }

    private suspend fun findLocalisationOrFallback(id: String): Localisation {
        val all = localisationRepository.getAll()
        return all.firstOrNull { it.id == id } ?: Localisation(id = id, name = id)
    }
}

sealed interface TestEvent {
    data object Saved : TestEvent
}

