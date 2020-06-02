package festusyuma.com.glaid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.map_view.*

/**
 * A simple [Fragment] subclass.
 */
class QuantityFragment : Fragment(R.layout.fragment_quantity) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onCustombtnclicked()
    }

    companion object {
        fun quantityInstance() = QuantityFragment()
    }

    fun onCustombtnclicked() {
        framelayoutFragment?.setPadding(0,0,0,0)
    }
}
