package pk.knpmi.barcode.presentation.test_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    viewModel: TestViewModel = hiltViewModel(),
) {
    val state by viewModel.state

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var localisationId by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.product, state.pendingNewProductBarcode) {
        val product = state.product
        if (product != null) {
            name = product.name
            category = product.category
            localisationId = product.localisationId
            quantityText = product.quantity.toString()
        } else if (state.pendingNewProductBarcode != null) {
            name = ""
            category = state.metadata?.categories?.firstOrNull().orEmpty()
            localisationId = state.localisation?.id.orEmpty()
            quantityText = "1"
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Test screen (fake repo) – ręczne skany")

            if (state.message != null) {
                Text(state.message!!)
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.barcodeInput,
                onValueChange = viewModel::onBarcodeInputChange,
                label = { Text("Barcode input (np. P-0001 albo L-0100)") },
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { viewModel.scanProduct() }, enabled = !state.isLoading) {
                    Text("Scan PRODUCT")
                }
                Button(onClick = { viewModel.scanLocalisation() }, enabled = !state.isLoading) {
                    Text("Scan LOCATION")
                }
            }

            HorizontalDivider()

            Text("Wybrany produkt:")
            Text(state.product?.toString() ?: "—")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.beginMoveSelectedProduct() },
                    enabled = state.product != null && !state.isLoading,
                ) {
                    Text("MOVE…")
                }
                Button(
                    onClick = { viewModel.deleteSelectedProduct() },
                    enabled = state.product != null && !state.isLoading,
                ) {
                    Text("DELETE")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                when {
                    state.awaitingMoveTargetLocalisationScan -> "Tryb MOVE: zeskanuj lokalizację docelową (Scan LOCATION)."
                    state.pendingNewProductBarcode != null -> "Nowy produkt: wypełnij formularz i kliknij SAVE."
                    state.product != null -> "Edycja produktu: zmień pola i kliknij SAVE."
                    else -> "—"
                },
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    value = category,
                    onValueChange = {},
                    label = { Text("Category") },
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.metadata?.categories?.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                expanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = quantityText,
                onValueChange = { quantityText = it },
                label = { Text("Quantity (Double)") },
                singleLine = true,
            )

            Button(
                onClick = {
                    val quantity = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    viewModel.createOrOverwriteProduct(
                        name = name,
                        category = category,
                        localisationId = localisationId,
                        quantity = quantity,
                    )
                },
                enabled = !state.isLoading && (state.pendingNewProductBarcode != null || state.product != null),
            ) {
                Text("SAVE")
            }

            HorizontalDivider()

            Text("Lokalizacja: ${state.localisation?.id ?: "—"} (${state.localisation?.name ?: "—"})")
            Text("Produkty w lokalizacji: ${state.productsAtLocalisation.size}")
        }
        items(state.productsAtLocalisation) { p ->
            Text("${p.id} • ${p.name} • qty=${p.quantity}")
        }
    }
}

