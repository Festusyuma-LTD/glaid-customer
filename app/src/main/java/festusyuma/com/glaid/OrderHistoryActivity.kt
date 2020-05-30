package festusyuma.com.glaid

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import festusyuma.com.glaid.utilities.DataServices
import festusyuma.com.glaid.utilities.OrderHistoryAdapter
import kotlinx.android.synthetic.main.activity_order_history.*

class OrderHistoryActivity : AppCompatActivity() {
    lateinit var orderHistoryAdapter: OrderHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val w: Window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        w.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        val layoutManager = LinearLayoutManager(this)
        orderHistoryAdapter = OrderHistoryAdapter(this, DataServices.orderHistoryList) {
            val productPageLink = Intent(this, OrderDetailsActivity::class.java)
//            productPageLink.putExtra(EXTRA_QUESTION, it.title)
            startActivity(productPageLink)

        }

        orderHistoryRecycler.layoutManager = layoutManager
        orderHistoryRecycler.adapter = orderHistoryAdapter
        // for performance when we know the layout sizes wont be changing
        orderHistoryRecycler.setHasFixedSize(true)
    }

    fun helpBackClick(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}
