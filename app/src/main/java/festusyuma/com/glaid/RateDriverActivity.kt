package festusyuma.com.glaid

import android.os.Bundle
import android.widget.RatingBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.request.OrderRequests
import festusyuma.com.glaid.requestdto.RatingRequest

class RateDriverActivity : Fragment(R.layout.activity_rate_driver) {

    private lateinit var doneBtn: ConstraintLayout
    private lateinit var ratingBar: RatingBar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val orderJson = arguments?.getString("order")
        val order = gson.fromJson(orderJson, Order::class.java)

        ratingBar = requireActivity().findViewById(R.id.ratingBar)
        doneBtn = requireActivity().findViewById(R.id.doneBtn)
        doneBtn.setOnClickListener {
            if (ratingBar.rating <= 0) OrderRequests(requireActivity()).showError("Rating cannot be 0")

            val orderId = order.id ?: return@setOnClickListener
            val ratingRequest = RatingRequest(orderId, ratingBar.rating.toDouble())

            OrderRequests(requireActivity()).rateDriver(ratingRequest) {
                requireActivity().supportFragmentManager.popBackStackImmediate()
            }
        }
    }
}
