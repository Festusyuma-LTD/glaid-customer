package festusyuma.com.glaid.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import festusyuma.com.glaid.R
import festusyuma.com.glaid.model.live.LiveAddress
import kotlinx.android.synthetic.main.search_address_result.view.*

class SearchAddressResultAdapter(private val context: Fragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var items: List<AutocompletePrediction> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_address_result, parent, false)
        return SearchViewHolder(view, context)
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

    fun submitList(searchAddResultList: List<AutocompletePrediction>) {
        items = searchAddResultList
        notifyDataSetChanged()
    }

    // view holder
    class SearchViewHolder(itemView: View, val context: Fragment): RecyclerView.ViewHolder(itemView){
        private val locationName: TextView = itemView.locationNameHolder
        private val locationAddress: TextView = itemView.locationAddressHolder
        private lateinit var liveAddress: LiveAddress

        // bind method
        fun bind(searchAddresses: AutocompletePrediction){
            itemView.setOnClickListener {
                selectAddress(searchAddresses)
            }

            locationName.text = searchAddresses.getPrimaryText(null)
            locationAddress.text = searchAddresses.getSecondaryText(null)
        }

        private fun selectAddress(place: AutocompletePrediction) {
            liveAddress = ViewModelProviders.of(context).get(LiveAddress::class.java)
            liveAddress.place.value = place
        }
    }
}