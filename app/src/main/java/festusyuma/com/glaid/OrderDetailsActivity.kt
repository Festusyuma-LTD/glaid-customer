package festusyuma.com.glaid

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.Order
import java.text.NumberFormat

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var order: Order

    private lateinit var quantity: TextView
    private lateinit var gasType: TextView
    private lateinit var locationTime: TextView
    private lateinit var deliveryAddress: TextView
    private lateinit var deliveryTime: TextView
    private lateinit var amount: TextView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        val w: Window = window
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        w.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        val orderJson = intent.getStringExtra("order")
        if (orderJson != null) {
            order = gson.fromJson(orderJson, Order::class.java)
        }else finish()

        initElements()
    }

    private fun initElements() {
        val numberFormatter = NumberFormat.getInstance()
        quantity = findViewById(R.id.quantity)
        gasType = findViewById(R.id.gasType)
        locationTime = findViewById(R.id.locationTime)
        deliveryAddress = findViewById(R.id.destination)
        deliveryTime = findViewById(R.id.destinationTime)
        amount = findViewById(R.id.paymentCost)
        status = findViewById(R.id.statusType)

        quantity.text = getString(R.string.formatted_quantity).format(order.quantity, order.gasUnit)
        gasType.text = order.gasType.capitalizeWords()
        deliveryAddress.text = order.deliveryAddress.address
        amount.text = getString(R.string.formatted_amount).format(numberFormatter.format(order.amount))
        status.text = getDeliveryStatusString(order.statusId)
    }

    private fun getDeliveryStatusString(statusId: Long): String {
        return when(statusId) {
            1L -> "Pending"
            2L -> "Driver assigned"
            3L -> "On the way"
            else -> "Delivered"
        }
    }

    fun backClick(view: View) {
        val intent = Intent(this, OrderHistoryActivity::class.java)
        startActivity(intent)
    }

    fun viewInvoiceClick(view: View) {}
    fun rateCustomerClick(view: View) {}
}
