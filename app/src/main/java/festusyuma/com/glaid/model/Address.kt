package festusyuma.com.glaid.model

data class Address (
    val id: Long? = null,
    var address: String,
    var type: String = "home",
    var lng: Double? = null,
    var lat: Double? = null
)