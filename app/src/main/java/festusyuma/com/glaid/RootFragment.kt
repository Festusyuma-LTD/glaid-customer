package festusyuma.com.glaid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_root.*

/**
 * A simple [Fragment] subclass.
 */
class RootFragment : Fragment(R.layout.fragment_root) {
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_root, container, false)
//    }
override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    dieselBtn.setOnClickListener{

        requireActivity().supportFragmentManager.beginTransaction()
//            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)// set animation
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.framelayoutFragment, DetailsFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }
}
}
