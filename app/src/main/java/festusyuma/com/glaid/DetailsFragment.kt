package festusyuma.com.glaid

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_details.*


/**
 * A simple [Fragment] subclass.
 */
class DetailsFragment : Fragment(R.layout.fragment_details) {

    //    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_root, container, false)
//    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        R.id.framelayoutFragment
        quantityBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
//            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)// set animation
                .setCustomAnimations(
                    R.anim.slide_up,
                    R.anim.slide_down,
                    R.anim.slide_up,
                    R.anim.slide_down
                )
                .replace(R.id.framelayoutFragment, QuantityFragment.quantityInstance())
                .addToBackStack(null)
                .commit()
            onCustombtnclicked()
        }
        // toggle button
        orderBtnOne.setOnClickListener() {
            toggleOrderButtonOne()
        }
        orderBtnTwo.setOnClickListener() {
            toggleOrderButtonTwo()
        }
    }
    fun toggleOrderButtonOne() {
        orderBtnTwo.isChecked = false
    }

    fun toggleOrderButtonTwo() {
        orderBtnOne.isChecked = false
    }
    fun onCustombtnclicked() {
        framelayoutFragment?.setPadding(0,0,0,0)
    }
    companion object {
        fun newInstance() = DetailsFragment()

    }
}
