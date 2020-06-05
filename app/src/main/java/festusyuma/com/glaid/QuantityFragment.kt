package festusyuma.com.glaid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_quantity.*
import kotlinx.android.synthetic.main.map_view.*

/**
 * A simple [Fragment] subclass.
 */
class QuantityFragment : Fragment(R.layout.fragment_quantity) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        onCustombtnclicked()
        LocationField.setOnClickListener {
            // load address fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down,R.anim.slide_up, R.anim.slide_down)
                .replace(R.id.framelayoutFragment, AddressFragment.addressInstance())
                .addToBackStack(null)
                .commit()
            // load search address fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_down, R.anim.slide_up)
                .replace(R.id.addressFramelayoutFragment, SearchAddressFragment.searchAddressInstance())
                .addToBackStack(null)
                .commit()

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
        val lp: FrameLayout.LayoutParams = FrameLayout.LayoutParams(0, 0)
        framelayoutFragment.layoutParams = lp
        print("::::::${framelayoutFragment.height}:::::")
    }

    companion object {
        fun quantityInstance() = QuantityFragment()
    }

//    fun onCustombtnclicked() {
//        framelayoutFragment?.setPadding(0,0,0,0)
//    }

}
