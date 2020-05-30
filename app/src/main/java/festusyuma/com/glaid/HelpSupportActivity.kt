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
import festusyuma.com.glaid.utilities.HelpSupportAdapter
import kotlinx.android.synthetic.main.activity_help_support.*

class HelpSupportActivity : AppCompatActivity() {
    lateinit var helpSupportAdapter: HelpSupportAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        val w: Window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

            )
        }
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        w.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        // defining an adapter using a custom recycler view
        helpSupportAdapter = HelpSupportAdapter(this, DataServices.questions) {
            val productPageLink = Intent(this, SupportFullPageActivity::class.java)
//            productPageLink.putExtra(EXTRA_QUESTION, it.title)
            startActivity(productPageLink)
        }
        val topQuestionlayoutManager = LinearLayoutManager(this)
        val paymentlayoutManager = LinearLayoutManager(this)

        topQuestionRecycler.layoutManager = topQuestionlayoutManager
        topQuestionRecycler.adapter = helpSupportAdapter
        // for performance when we know the layout sizes wont be changing
        topQuestionRecycler.setHasFixedSize(true)

        paymentRecycler.layoutManager = paymentlayoutManager
        paymentRecycler.adapter = helpSupportAdapter
        // for performance when we know the layout sizes wont be changing
        paymentRecycler.setHasFixedSize(true)
    }

    fun helpBackClick(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}
