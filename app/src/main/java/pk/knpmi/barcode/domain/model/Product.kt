package pk.knpmi.barcode.domain.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val localisationId: String,
    val quantity: Double,
    val date: Long,
)