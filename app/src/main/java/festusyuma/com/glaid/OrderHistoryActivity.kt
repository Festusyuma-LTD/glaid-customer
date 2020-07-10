package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.reflect.TypeToken
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.utilities.OrderHistoryAdapter
import kotlinx.android.synthetic.main.activity_order_history.*

class OrderHistoryActivity : AppCompatActivity() {

    lateinit var dataPref: SharedPreferences
    lateinit var orderHistoryAdapter: OrderHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        dataPref = getSharedPreferences("cached_data", Context.MODE_PRIVATE)

        val w: Window = window
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        w.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        val layoutManager = LinearLayoutManager(this)
        val typeToken = object: TypeToken<MutableList<Order>>(){}.type
        val ordersJson = dataPref.getString(getString(R.string.sh_orders), null)

        if (ordersJson != null) {
            val orders: MutableList<Order> = gson.fromJson(ordersJson, typeToken)
            orderHistoryAdapter = OrderHistoryAdapter(this, orders) {
                val orderDetails = Intent(this, OrderDetailsActivity::class.java)
                orderDetails.putExtra("order", gson.toJson(it))
                startActivity(orderDetails)
            }

            orderHistoryRecycler.layoutManager = layoutManager
            orderHistoryRecycler.adapter = orderHistoryAdapter
            // for performance when we know the layout sizes wont be changing
            orderHistoryRecycler.setHasFixedSize(true)
        }
    }

    fun helpBackClick(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}
