package festusyuma.com.glaid

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import festusyuma.com.glaid.helpers.getFirst
import festusyuma.com.glaid.model.User
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_root.*

/**
 * A simple [Fragment] subclass.
 */
class RootFragment : Fragment(R.layout.fragment_root) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sharedPref = this.activity?.getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        val userJson = sharedPref?.getString("userDetails", "null")

        if (userJson != null) {
            val user = gson.fromJson(userJson, User::class.java)
            greeting.text = getString(R.string.home_greeting_intro_text).format(user.fullName.getFirst())
        }

        dieselBtn.setOnClickListener{
            //todo get diesel details
            openGasDetails()
        }

        gasBtn.setOnClickListener{
            //todo get gas details
            openGasDetails()
        }

    }

    private fun openGasDetails() {

        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.framelayoutFragment, DetailsFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }
}
