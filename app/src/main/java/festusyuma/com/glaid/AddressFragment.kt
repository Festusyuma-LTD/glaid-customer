package festusyuma.com.glaid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.map_view.*

/**
 * A simple [Fragment] subclass.
 */
class AddressFragment : Fragment(R.layout.fragment_address) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//
    }



    companion object {
        fun addressInstance() = AddressFragment()
    }

//    fun onCustombtnclicked() {
//        framelayoutFragment?.setPadding(0,0,0,0)
//    }

}
