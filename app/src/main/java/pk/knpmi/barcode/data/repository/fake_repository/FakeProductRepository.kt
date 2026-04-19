package pk.knpmi.barcode.data.repository.fake_repository

import android.util.Log
import pk.knpmi.barcode.domain.model.Product
import pk.knpmi.barcode.domain.model.ProductMetadata
import pk.knpmi.barcode.domain.repository.ProductRepository

class FakeProductRepository : ProductRepository {

    private val tag = "FakeProductRepository" // Tag used by Logcat to group messages.

    // Shared in-memory "table" with products.
    private val products = FakeInMemoryStore.products

    override suspend fun create(product: Product): Product {
        // We treat Product.id as the scanned barcode (String).
        // If an item with the same id exists, we overwrite it; otherwise we append.
        val existingIndex = products.indexOfFirst { it.id == product.id }
        if (existingIndex >= 0) {
            products[existingIndex] = product
            Log.d(tag, "create: overwrote existing id=${product.id} product=$product")
        } else {
            products.add(product)
            Log.d(tag, "create: added id=${product.id} product=$product")
        }

        return product
    }

    override suspend fun getByBarcode(barcode: String): Product? {
        // The scanner returns a String; here barcode == Product.id.
        val found = products.firstOrNull { it.id == barcode }
        Log.d(tag, "getByBarcode: barcode=$barcode -> found=$found")
        return found
    }

    override suspend fun getMetadata(): ProductMetadata {
        // Hardcoded values to simulate dropdowns/filters without calling backend.
        val metadata =
            ProductMetadata(
                producers = listOf("Brak (mock)", "Acme Foods", "GoodCo"),
                categories = listOf("Nabiał", "Pieczywo", "Napoje", "Inne"),
            )
        Log.d(tag, "getMetadata: $metadata")
        return metadata
    }

    override suspend fun update(product: Product): Product {
        // Find product by id and replace it (or add if it doesn't exist).
        val index = products.indexOfFirst { it.id == product.id }
        if (index < 0) {
            products.add(product)
            Log.d(tag, "update: id=${product.id} not found -> added product=$product")
            return product
        }

        products[index] = product
        Log.d(tag, "update: id=${product.id} updated product=$product")
        return product
    }

    override suspend fun delete(id: String) {
        // Remove by id; removeAll returns true if anything was removed.
        val removed = products.removeAll { it.id == id }
        Log.d(tag, "delete: id=$id removed=$removed")
    }
}

