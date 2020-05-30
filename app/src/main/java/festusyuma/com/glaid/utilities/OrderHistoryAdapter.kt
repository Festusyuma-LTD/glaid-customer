package festusyuma.com.glaid.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import festusyuma.com.glaid.R

class OrderHistoryAdapter(
    val context: Context,
    val orderHistories: List<OrderHistory>,
    val itemClicked: (OrderHistory) -> Unit
) :
    RecyclerView.Adapter<OrderHistoryAdapter.Holder>() {

    //Add a view holder
    inner class Holder(itemView: View?, val itemClicked: (OrderHistory) -> Unit) :
        RecyclerView.ViewHolder(itemView!!) {
        private val literView = itemView?.findViewById<TextView>(R.id.literView)
        private val oilType = itemView?.findViewById<TextView>(R.id.oilType)
        private val addressView = itemView?.findViewById<TextView>(R.id.addressView)
        private val deliverStatus = itemView?.findViewById<TextView>(R.id.deliverStatus)


        fun bindData(context: Context, orderHistory: OrderHistory) {
            //getting a resource id for use
//            val resourceId =
//                context.resources.getIdentifier(category.image, "drawable", context.packageName)
//            categoryImage?.setImageResource(resourceId)
            literView?.text = orderHistory.literValue
            oilType?.text = orderHistory.oilType
            addressView?.text = orderHistory.address
            deliverStatus?.text = orderHistory.deliverStatus
            deliverStatus?.setTextColor(orderHistory.deliveryStatusColor())
            itemView.setOnClickListener {
                itemClicked(orderHistory)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.order_history_item, parent, false)
        return Holder(view, itemClicked)
    }

    override fun getItemCount(): Int {
        return orderHistories.count()
    }

    // Bind the inner class Holder here to reuse
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindData(context, orderHistories[position])
    }
}