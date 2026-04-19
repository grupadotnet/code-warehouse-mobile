package pk.knpmi.barcode.domain.repository

import pk.knpmi.barcode.domain.model.Product
import pk.knpmi.barcode.domain.model.ProductMetadata

interface ProductRepository {

    /** Creates a new product */
    suspend fun create(product: Product): Product

    /** Loads a single product by its barcode */
    suspend fun getByBarcode(barcode: String): Product?

    /** Returns selectable producers and categories for forms */
    suspend fun getMetadata(): ProductMetadata

    /** Updates an existing product, e.g. quantity or location  */
    suspend fun update(product: Product): Product

    /** Removes a product */
    suspend fun delete(id: String)
}
