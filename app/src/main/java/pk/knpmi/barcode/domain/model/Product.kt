package pk.knpmi.barcode.domain.model

data class Product(
    val id: Long,
    val barcode: String,
    val name: String,
    val category: String,
    val localisationId: Long,
    val quantity: Double,
    val date: Long,
)