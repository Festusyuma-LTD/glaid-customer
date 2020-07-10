package festusyuma.com.glaid.utilities

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import festusyuma.com.glaid.R
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.Order

class OrderHistoryAdapter(
    val context: Context,
    private val orders: List<Order>,
    private val itemClicked: (Order) -> Unit
) :
    RecyclerView.Adapter<OrderHistoryAdapter.Holder>() {

    //Add a view holder
    inner class Holder(itemView: View, val itemClicked: (Order) -> Unit): RecyclerView.ViewHolder(itemView) {
        private val quantity: TextView = itemView.findViewById(R.id.quantity)
        private val gasType: TextView = itemView.findViewById(R.id.gasType)
        private val addressView: TextView = itemView.findViewById(R.id.deliveryAddress)
        private val deliverStatus: TextView = itemView.findViewById(R.id.status)


        fun bindData(context: Context, order: Order) {
            quantity.text = context.getString(R.string.formatted_quantity).format(order.quantity, order.gasUnit)
            gasType.text = order.gasType.capitalizeWords()
            addressView.text = order.deliveryAddress.address
            deliverStatus.text = getDeliveryStatusString(order.statusId)
            deliverStatus.setTextColor(getDeliveryStatusColour(order.statusId))

            itemView.setOnClickListener {
                itemClicked(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.order_history_item, parent, false)
        return Holder(view, itemClicked)
    }

    override fun getItemCount(): Int {
        return orders.count()
    }

    // Bind the inner class Holder here to reuse
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindData(context, orders[position])
    }

    private fun getDeliveryStatusString(statusId: Long): String {
        return when(statusId) {
            1L -> "Pending"
            2L -> "Driver assigned"
            3L -> "On the way"
            else -> "Delivered"
        }
    }

    private fun getDeliveryStatusColour(statusId: Long): Int {
        return when(statusId) {
            1L -> Color.parseColor("#FC7400")
            2L -> Color.parseColor("#FC7400")
            3L -> Color.parseColor("#27AE60")
            else -> Color.parseColor("#4E007C")
        }
    }
}