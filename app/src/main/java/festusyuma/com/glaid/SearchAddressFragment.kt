package festusyuma.com.glaid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_quantity.*
import kotlinx.android.synthetic.main.map_view.*

/**
 * A simple [Fragment] subclass.
 */
class SearchAddressFragment : Fragment(R.layout.fragment_search_address) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        onCustombtnclicked()
//        LocationField.setOnClickListener {
//            requireActivity().supportFragmentManager.beginTransaction()
//                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
//                .replace(R.id.addressFramelayoutFragment, QuantityFragment.quantityInstance())
//                .addToBackStack(null)
//                .commit()
//        }

    }

    companion object {
        fun searchAddressInstance() = SearchAddressFragment()
    }

//    fun onCustombtnclicked() {
//        framelayoutFragment?.setPadding(0,0,0,0)
//    }

}
