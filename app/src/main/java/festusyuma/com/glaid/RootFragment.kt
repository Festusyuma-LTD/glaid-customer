package festusyuma.com.glaid

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.helpers.getFirst
import festusyuma.com.glaid.model.User
import festusyuma.com.glaid.model.live.LiveOrder

/**
 * A simple [Fragment] subclass.
 */
class RootFragment : Fragment(R.layout.fragment_root) {

    private lateinit var liveOrder: LiveOrder

    private lateinit var greeting: TextView
    private lateinit var dieselBtn: ConstraintLayout
    private lateinit var gasBtn: ConstraintLayout

    private lateinit var errorMsg: TextView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        greeting = requireActivity().findViewById(R.id.greeting)
        dieselBtn = requireActivity().findViewById(R.id.dieselBtn)
        gasBtn = requireActivity().findViewById(R.id.gasBtn)

        errorMsg = requireActivity().findViewById(R.id.errorMsg)

        val sharedPref = requireActivity().getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        val userJson = sharedPref?.getString("userDetails", "null")

        if (userJson != null) {
            val user = gson.fromJson(userJson, User::class.java)
            greeting.text = getString(R.string.home_greeting_intro_text).format(user.fullName?.getFirst())
        }

        dieselBtn.setOnClickListener{
            openGasDetails("diesel")
        }

        gasBtn.setOnClickListener{
            openGasDetails("gas")
        }

    }

    private fun openGasDetails(type: String) {
        val detailsFragment = DetailsFragment()
        val args = Bundle()
        args.putString("type", type)
        detailsFragment.arguments = args

        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.frameLayoutFragment, detailsFragment)
            .addToBackStack(null)
            .commit()
    }

    fun hideError(view: View) {
        errorMsg.visibility = View.INVISIBLE
    }
}
