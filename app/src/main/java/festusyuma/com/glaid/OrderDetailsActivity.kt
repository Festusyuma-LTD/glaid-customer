package festusyuma.com.glaid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.Order
import kotlinx.android.synthetic.main.activity_order_details.*
import org.threeten.bp.format.DateTimeFormatter
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
    private lateinit var driverName: TextView
    private lateinit var rateDriverBtn: TextView
    private lateinit var driverRatingBar: RatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        val w: Window = window
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
        driverName = findViewById(R.id.driverNameText)
        rateDriverBtn = findViewById(R.id.rateDriverBtn)
        driverRatingBar = findViewById(R.id.driverRating)

        quantity.text = getString(R.string.formatted_quantity).format(order.quantity, order.gasUnit)
        gasType.text = order.gasType.capitalizeWords()
        deliveryAddress.text = order.deliveryAddress.address
        amount.text = getString(R.string.formatted_amount).format(numberFormatter.format(order.amount))
        status.text = getDeliveryStatusString(order.statusId)
        locationTime.text = order.tripStarted?.format(DateTimeFormatter.ofPattern("HH:mm"))
        destinationTime.text = order.tripEnded?.format(DateTimeFormatter.ofPattern("HH:mm"))
        driverName.text = getString(R.string.order_details_driver_name).format(
            order.driver?.fullName?.capitalizeWords()
        )

        val driverRating = order.driverRating

        if (order.statusId != OrderStatusCode.DELIVERED) {
            rateDriverBtn.visibility = View.GONE
        }

        if (driverRating != null) {
            rateDriverBtn.visibility = View.GONE
            driverRatingBar.visibility = View.VISIBLE
            driverRatingBar.rating = driverRating.toFloat()
        }
    }

    private fun getDeliveryStatusString(statusId: Long): String {
        return when(statusId) {
            OrderStatusCode.PENDING -> "Pending"
            OrderStatusCode.DRIVER_ASSIGNED -> "Driver assigned"
            OrderStatusCode.ON_THE_WAY -> "On the way"
            OrderStatusCode.PENDING_PAYMENT -> "Pending payment"
            OrderStatusCode.FAILED -> "Failed"
            else -> "Delivered"
        }
    }

    fun backClick(view: View) {
        finish()
    }

    fun viewInvoiceClick(view: View) {
        val intent = Intent(this, ReceiptActivity::class.java)
        intent.putExtra("order", gson.toJson(order))
        startActivity(intent)
    }

    fun rateCustomerClick(view: View) {
        val bundle = Bundle()
        val ratingFragment = RateDriverActivity()

        bundle.putString("order", gson.toJson(order))
        ratingFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.ratingFragment, ratingFragment)
            .addToBackStack(null)
            .commit()
    }
}
