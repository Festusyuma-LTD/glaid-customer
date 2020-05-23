package festusyuma.com.glaid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
//override fun onActivityCreated(savedInstanceState: Bundle?) {
//    super.onActivityCreated(savedInstanceState)
//    textView42.bringToFront()
//}
companion object {
    fun newInstance() = DetailsFragment()

}
}
