package festusyuma.com.glaid

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import festusyuma.com.glaid.model.SearchAddresses
import festusyuma.com.glaid.model.live.LiveOrder
import festusyuma.com.glaid.utilities.SearchAddressResultAdapter
import kotlinx.android.synthetic.main.fragment_address.*
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 */
class AddressFragment : Fragment(R.layout.fragment_address) {

    private lateinit var liveOrder: LiveOrder

    private lateinit var searchAdapter: SearchAddressResultAdapter
    private lateinit var searchResult: RecyclerView
    private lateinit var searchInput: EditText
    private val placesToken = AutocompleteSessionToken.newInstance();

    private lateinit var placesClient: PlacesClient

    private var searching = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
        liveOrder = ViewModelProviders.of(requireActivity()).get(LiveOrder::class.java)

        initLiveSearch()
        initRecyclerView()
        addDataSet()
    }

    private fun initLiveSearch() {
        searchInput = requireActivity().findViewById(R.id.searchInput)

        searchInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        search()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun search() {
        if (searching) {
            return
        }else searching = true
        val query = searchInput.text.toString()

        Log.v("ApiLog", "Search res: $query")
        placesClient.findAutocompletePredictions(
            FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(placesToken)
                .build()
        ).addOnCompleteListener {
            searching = false
        }.addOnSuccessListener {
            for (prediction in it.autocompletePredictions) {
                Log.v("ApiLog", "Search res: $prediction")
            }
        }.addOnFailureListener{
            Log.v("ApiLog", "Search res: ${it.message}")
        }
    }

    private fun addDataSet(){
        val data = listOf(
            SearchAddresses(
                "Festusyuma",
                address = "Plot 3058 ossy agoms, apple estate, amuwo odofin, lagos",
                lat = 1.1231313,
                lng = 1.1231313
            ),
            SearchAddresses(
                address = "Plot 3058 ossy agoms, apple estate, amuwo odofin, lagos",
                lat = 1.1231313,
                lng = 1.1231313
            )
        )

        searchAdapter.submitList(data)
    }

    private fun initRecyclerView(){
        searchResult = requireActivity().findViewById(R.id.addressSearchResult)

        addressSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            searchAdapter = SearchAddressResultAdapter()
            adapter = searchAdapter
        }
    }
}
