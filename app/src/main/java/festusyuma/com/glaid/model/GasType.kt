package festusyuma.com.glaid.model

data class GasType (
    val id: Long,
    var type: String,
    var price: Double,
    var unit: String,
    var hasFixedQuantities: Boolean = false,
    var fixedQuantities: MutableList<GasTypeQuantities> = mutableListOf()
)