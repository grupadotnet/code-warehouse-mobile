package pk.knpmi.barcode.domain.model

// Will be used for displaying in drop down menu, for quick select
data class ProductMetadata(
    val producers: List<String>,
    val categories: List<String>,
)