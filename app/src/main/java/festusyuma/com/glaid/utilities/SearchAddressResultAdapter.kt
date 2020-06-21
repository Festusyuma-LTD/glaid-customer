package festusyuma.com.glaid.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import festusyuma.com.glaid.R
import festusyuma.com.glaid.model.SearchAddresses
import kotlinx.android.synthetic.main.search_address_result.view.*

class SearchAddressResultAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var items: List<SearchAddresses> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.search_address_result, parent, false)
        )
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is SearchViewHolder ->{
                holder.bind(items.get(position))
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
    class SearchViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView){
        val location_name: TextView? = itemView.locationNameHolder
        val location_address: TextView = itemView.locationAddressHolder

        // bind method
        fun bind(searchAddresses: SearchAddresses){
            location_name?.text = searchAddresses.locationName
            location_address.text = searchAddresses.locationAddress
        }
    }
}