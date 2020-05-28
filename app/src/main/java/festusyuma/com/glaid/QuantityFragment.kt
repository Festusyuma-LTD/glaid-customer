package festusyuma.com.glaid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_details.*

/**
 * A simple [Fragment] subclass.
 */
class QuantityFragment : Fragment(R.layout.fragment_quantity) {

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_root, container, false)
//    }
override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    fragmentConstraint?.setPadding(0,0,0,0)
    // toggle button
//    orderBtnOne.setOnClickListener(){
//        toggleOrderButtonOne()
//    }
//    orderBtnTwo.setOnClickListener(){
//        toggleOrderButtonTwo()
//    }
}
//    fun toggleOrderButtonOne (){
//        orderBtnTwo.isChecked = false
//    }
//    fun toggleOrderButtonTwo (){
//        orderBtnOne.isChecked = false
//    }
    companion object {
        fun quantityInstance() = QuantityFragment()

    }
}
