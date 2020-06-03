package festusyuma.com.glaid

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.map_view.*


/**
 * A simple [Fragment] subclass.
 */
class DetailsFragment : Fragment(R.layout.fragment_details) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        quantityBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .replace(R.id.framelayoutFragment, QuantityFragment.quantityInstance())
                .addToBackStack(null)
                .commit()
        }

        /*
        // toggle button
        orderBtnOne.setOnClickListener() {
            toggleOrderButtonOne()
        }
        orderBtnTwo.setOnClickListener() {
            toggleOrderButtonTwo()
        }*/
    }
    /*fun toggleOrderButtonOne() {
        orderBtnTwo.isChecked = false
    }

    fun toggleOrderButtonTwo() {
        orderBtnOne.isChecked = false
    }*/
    fun onCustombtnclicked() {
        framelayoutFragment?.setPadding(0,0,0,0)
    }
    companion object {
        fun newInstance() = DetailsFragment()

    }
}
