package festusyuma.com.glaid.utilities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import festusyuma.com.glaid.R
import festusyuma.com.glaid.model.SearchAddresses
import kotlinx.android.synthetic.main.search_address_result.view.*

class SearchAddressResultAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var items: List<SearchAddresses> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_address_result, parent, false)
        view.setOnClickListener {
            Log.v("ApiLog", "CLicked me item")
        }

        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is SearchViewHolder ->{
                holder.bind(items[position])
            }
        }
    }
    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(searchAddResultList: List<SearchAddresses>) {
        items = searchAddResultList
    }

    // view holder
    class SearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val locationName: TextView = itemView.locationNameHolder
        private val locationAddress: TextView = itemView.locationAddressHolder

        // bind method
        fun bind(searchAddresses: SearchAddresses){
            if (searchAddresses.locationName != null) {
                locationName.text = searchAddresses.locationName
            }else locationName.visibility = View.GONE

            locationAddress.text = searchAddresses.address
        }
    }
}