package festusyuma.com.glaid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.model.Address
import festusyuma.com.glaid.model.live.LiveAddress
import festusyuma.com.glaid.model.live.LiveOrder
import festusyuma.com.glaid.utilities.SearchAddressResultAdapter
import kotlinx.android.synthetic.main.fragment_address.*
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */
class AddressFragment : Fragment(R.layout.fragment_address) {

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView
    private var operationRunning = false

    private var token: String? = ""
    private lateinit var queue: RequestQueue

    private lateinit var liveOrder: LiveOrder
    private lateinit var liveAddress: LiveAddress
    private var currentSearchResult: List<AutocompletePrediction> = listOf()

    private lateinit var addHomeAddressBtn : ConstraintLayout
    private lateinit var addOfficeAddress : ConstraintLayout

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
        liveAddress = ViewModelProviders.of(this).get(LiveAddress::class.java)

        queue = Volley.newRequestQueue(requireContext())
        val authPref = requireActivity().getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        if (authPref.contains(getString(R.string.auth_key_name))) {
            token = authPref.getString(getString(R.string.auth_key_name), token)
        }

        initLoadingAndError()
        initLiveSearch()
        initRecyclerView()
        initAddressUpdate()
        addAddressBtn()
    }

    private fun initLoadingAndError() {
        loadingCover = requireActivity().findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = requireActivity().findViewById(R.id.errorMsg)
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
        val query = searchInput.text.toString()
        placesClient.findAutocompletePredictions(
            FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(placesToken)
                .build()
        ).addOnCompleteListener {
            searching = false
        }.addOnSuccessListener {
            currentSearchResult = it.autocompletePredictions
            searchAdapter.submitList(it.autocompletePredictions)
        }.addOnFailureListener{
            Log.v("ApiLog", "Search res: ${it.message}")
        }
    }

    private fun initRecyclerView(){
        searchResult = requireActivity().findViewById(R.id.addressSearchResult)
        addressSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            searchAdapter = SearchAddressResultAdapter(this@AddressFragment)
            adapter = searchAdapter
        }
    }

    private fun initAddressUpdate() {
        liveAddress = ViewModelProviders.of(this).get(LiveAddress::class.java)
        liveAddress.place.observe(viewLifecycleOwner, Observer{
            //todo update address
            getPlaceDetails(it.placeId).addOnSuccessListener {placeResponse ->
                val deliveryType = liveOrder.addressType.value?: "home"
                val address = convertPlaceToAddress(placeResponse.place, deliveryType)
                liveOrder.deliveryAddress.value = address
                requireActivity().supportFragmentManager.popBackStackImmediate()
            }
        })
    }

    private fun addAddressBtn() {
        addHomeAddressBtn = requireActivity().findViewById(R.id.addHomeAddressBtn)
        addOfficeAddress = requireActivity().findViewById(R.id.addOfficeAddressBtn)

        addHomeAddressBtn.setOnClickListener { addAddress("home") }
        addOfficeAddress.setOnClickListener { addAddress("business") }
    }

    private fun getPlaceDetails(placeId: String): Task<FetchPlaceResponse> {
        val placeFields = listOf(
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.NAME
        )
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        return placesClient.fetchPlace(request)
    }

    private fun addAddress(type: String) {
        if (!operationRunning) {
            setLoading(true)

            if (currentSearchResult.isNotEmpty()) {
                val placeId = currentSearchResult[0].placeId
                getPlaceDetails(placeId).addOnSuccessListener {
                    val address = convertPlaceToAddress(it.place, type)

                    if (address == null) {
                        showError("An error occurred")
                    }else saveAddress(address)
                    Log.v("ApiLog", "$address")

                }.addOnFailureListener{
                    showError("An error occurred")
                }
            }else { showError("No address found") }
        }
    }

    private fun saveAddress(address: Address) {
        val reqObj = JSONObject(gson.toJson(address))
        val req = object : JsonObjectRequest(
            Method.POST,
            Api.ADD_ADDRESS,
            reqObj,
            Response.Listener { response ->
                if (response.getInt("status") == 200) {
                    Toast.makeText(requireContext(), "${address.type} address added", Toast.LENGTH_SHORT).show()
                }else {
                    showError(response.getString("message"))
                }

                setLoading(false)
            },
            Response.ErrorListener { response->
                if (response.networkResponse == null) showError(getString(R.string.internet_error_msg)) else {
                    if (response.networkResponse.statusCode == 403) {
                        logout()
                    }else showError(getString(R.string.api_error_msg))
                }

                setLoading(false)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $token"
                )
            }
        }

        req.tag = "add_address"
        queue.add(req)
    }

    private fun convertPlaceToAddress(place: Place, type: String): Address? {
        val location = place.latLng

        if (location == null){
            showError("An error occurred")
            return null
        }

        return Address(
            address = place.address?: place.name?: "${location.longitude}, ${location.latitude}",
            lat = location.latitude,
            lng = location.longitude,
            type = type
        )
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            loadingCover.visibility = View.VISIBLE
            loadingAvi.show()
            operationRunning = true
        }else {
            loadingCover.visibility = View.GONE
            operationRunning = false
        }
    }

    private fun showError(msg: String) {
        errorMsg.text = msg
        errorMsg.visibility = View.VISIBLE
    }

    private fun logout() {
        val sharedPref = requireActivity().getSharedPreferences("auth_token", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(getString(R.string.auth_key_name))
            commit()
        }

        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finishAffinity()
    }

    override fun onPause() {
        super.onPause()
        queue.cancelAll("add_address")
    }
}
