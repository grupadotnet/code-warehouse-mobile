package pk.knpmi.barcode.domain.repository

import pk.knpmi.barcode.domain.model.Localisation
import pk.knpmi.barcode.domain.model.Product

interface LocalisationRepository {

    /** Returns all locations */
    suspend fun getAll(): List<Localisation>

    /** Returns every product stored at the given location */
    suspend fun getProducts(localisationId: String): List<Product>
}
