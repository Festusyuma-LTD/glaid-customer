package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.helpers.Dashboard
import festusyuma.com.glaid.helpers.getFirst
import festusyuma.com.glaid.model.User
import kotlinx.android.synthetic.main.activity_forgot_pass_otp_final.*
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
            openGasDetails("diesel")
        }

        gasBtn.setOnClickListener{
            openGasDetails("gas")
        }

    }

    private fun openGasDetails(type: String) {
        val detailsFragment = DetailsFragment.newInstance()
        val args = Bundle()
        args.putString("type", type)
        detailsFragment.arguments = args

        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.framelayoutFragment, detailsFragment)
            .addToBackStack(null)
            .commit()
    }

    fun hideError(view: View) {
        errorMsg.visibility = View.INVISIBLE
    }
}
