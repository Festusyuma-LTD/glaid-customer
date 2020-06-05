package festusyuma.com.glaid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
    }
    fun addCardClick(view: View) {
        var addCardIntent = Intent(this, AddCardActivity::class.java)
        startActivity(addCardIntent)
    }
}
