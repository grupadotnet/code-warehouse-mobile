package pk.knpmi.barcode.data.repository.fake_repository

import android.util.Log
import pk.knpmi.barcode.domain.model.Localisation
import pk.knpmi.barcode.domain.model.Product
import pk.knpmi.barcode.domain.repository.LocalisationRepository

/**
 * Simple in-memory fake for locations.
 */
class FakeLocalisationRepository : LocalisationRepository {

    private val tag = "FakeLocalisationRepository" // Tag used by Logcat to group messages.

    // Shared in-memory "table" with locations.
    private val localisations = FakeInMemoryStore.localisations
    // Shared in-memory "table" with products (needed for getProducts()).
    private val products = FakeInMemoryStore.products

    override suspend fun getAll(): List<Localisation> {
        // Return a copy so callers can't mutate our internal list.
        Log.d(tag, "getAll: count=${localisations.size}")
        return localisations.toList()
    }

    override suspend fun getProducts(localisationId: String): List<Product> {
        // Filter products by the location id (like GET /api/locations/{id}/products).
        val result = products.filter { it.localisationId == localisationId }

        Log.d(tag, "getProducts: localisationId=$localisationId -> count=${result.size}")
        return result
    }
}

