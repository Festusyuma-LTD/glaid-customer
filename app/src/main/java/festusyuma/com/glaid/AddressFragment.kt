package festusyuma.com.glaid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import festusyuma.com.glaid.utilities.SearchAddressResultAdapter
import kotlinx.android.synthetic.main.fragment_address.*
import kotlinx.android.synthetic.main.map_view.*

/**
 * A simple [Fragment] subclass.
 */
class AddressFragment : Fragment(R.layout.fragment_address) {
    private lateinit var searchAdapter: SearchAddressResultAdapter
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerView()
        addDataSet()
    }



    companion object {
        fun addressInstance() = AddressFragment()
    }

    private fun addDataSet(){
        val data = SearchAddDataSource.createDataSet()
        searchAdapter.submitList(data)
    }
    private fun initRecyclerView(){
        search_address_recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            searchAdapter = SearchAddressResultAdapter()
            adapter = searchAdapter

        }
    }

//    fun onCustombtnclicked() {
//        framelayoutFragment?.setPadding(0,0,0,0)
//    }

}
