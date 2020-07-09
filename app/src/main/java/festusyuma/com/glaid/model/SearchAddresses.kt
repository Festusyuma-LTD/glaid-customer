package festusyuma.com.glaid.model

data class SearchAddresses(
    var locationName: String? = null,
    val address: String,
    val lat: Double,
    val lng: Double
)