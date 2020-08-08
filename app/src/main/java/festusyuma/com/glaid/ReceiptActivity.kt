package festusyuma.com.glaid

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import festusyuma.com.glaid.helpers.capitalizeWords
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.model.User
import org.threeten.bp.format.DateTimeFormatter
import java.text.NumberFormat

class ReceiptActivity : AppCompatActivity() {

    private lateinit var user: User
    private lateinit var order: Order
    private lateinit var dataPref: SharedPreferences

    private lateinit var orderNumber: TextView
    private lateinit var orderDate: TextView
    private lateinit var orderGasType: TextView
    private lateinit var orderQuantity: TextView
    private lateinit var orderAmount: TextView
    private lateinit var subTotal: TextView
    private lateinit var orderDeliveryPrice: TextView
    private lateinit var orderTaxPrice: TextView
    private lateinit var orderTotalPrice: TextView
    private lateinit var customerName: TextView
    private lateinit var customerAddress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        val orderJson = intent.getStringExtra("order")
        if (orderJson != null) {
            order = gson.fromJson(orderJson, Order::class.java)
        }else finish()

        dataPref = getSharedPreferences(getString(R.string.cached_data), Context.MODE_PRIVATE)
        if (dataPref.contains(getString(R.string.sh_user_details))) {
            val userJson = dataPref.getString(getString(R.string.sh_user_details), null)
            if (userJson != null) {
                user = gson.fromJson(userJson, User::class.java)
            }else finish()
        }else finish()

        initElements()
    }

    private fun initElements() {
        val numberFormatter = NumberFormat.getInstance()
        val amount = getString(R.string.formatted_amount).format(
            numberFormatter.format(order.amount)
        )
        val totalAmount = getString(R.string.formatted_amount).format(
            numberFormatter.format(order.amount + order.deliveryPrice + order.tax)
        )
        val delivery = getString(R.string.formatted_amount).format(
            numberFormatter.format(order.deliveryPrice)
        )
        val tax = getString(R.string.formatted_amount).format(
            numberFormatter.format(order.tax)
        )

        val orderNumberStr = order.id.toString()
        val orderNumberFormatted = if (orderNumberStr.length < 10) {
            "0".repeat(10 - orderNumberStr.length) + orderNumberStr
        }else orderNumberStr

        orderNumber = findViewById(R.id.orderNumber)
        orderDate = findViewById(R.id.orderDateField)
        orderGasType = findViewById(R.id.orderGasUnit)
        orderQuantity = findViewById(R.id.orderQuantity)
        orderAmount = findViewById(R.id.orderAmount)
        subTotal = findViewById(R.id.subTotal)
        orderDeliveryPrice = findViewById(R.id.orderDeliveryPrice)
        orderTaxPrice = findViewById(R.id.orderTaxPrice)
        orderTotalPrice = findViewById(R.id.orderTotalPrice)
        customerName = findViewById(R.id.customerName)
        customerAddress = findViewById(R.id.customerAddress)

        orderNumber.text = getString(R.string.order_number).format(orderNumberFormatted)
        orderDate.text = order.created?.format(DateTimeFormatter.ofPattern("hh:mm a - dd/MM/yyyy"))
        orderGasType.text = order.gasType.capitalizeWords()
        orderQuantity.text = getString(R.string.formatted_quantity).format(
            order.quantity,
            order.gasUnit
        )
        orderAmount.text = amount
        subTotal.text = amount
        orderDeliveryPrice.text = delivery
        orderTaxPrice.text = tax
        orderTotalPrice.text = totalAmount
        customerName.text = user.fullName?.capitalizeWords()
        customerAddress.text = order.deliveryAddress.address
    }
}
