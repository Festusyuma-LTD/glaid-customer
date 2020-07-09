package festusyuma.com.glaid

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.jakewharton.threetenabp.AndroidThreeTen
import com.wang.avi.AVLoadingIndicatorView
import festusyuma.com.glaid.model.Address
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.model.live.LiveOrder
import kotlinx.android.synthetic.main.fragment_quantity.*
import org.threeten.bp.LocalDateTime
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class QuantityFragment : Fragment(R.layout.fragment_quantity), DatePickerDialog.OnDateSetListener,  TimePickerDialog.OnTimeSetListener{

    private lateinit var loadingCover: ConstraintLayout
    private lateinit var loadingAvi: AVLoadingIndicatorView
    private lateinit var errorMsg: TextView
    private var operationRunning = false
    private lateinit var queue: RequestQueue

    private lateinit var liveOrder: LiveOrder

    private lateinit var quantity: EditText
    private lateinit var gasUnit: TextView

    private lateinit var homeAddressToggle: ToggleButton
    private lateinit var businessAddressToggle: ToggleButton
    private lateinit var payOnDeliveryToggle: ToggleButton
    private lateinit var scheduleLaterToggle: ToggleButton

    private lateinit var paymentTimeLbl: TextView
    private lateinit var paymentTimeToggleGroup: LinearLayout

    private lateinit var datePicker: DatePickerDialog
    private lateinit var timePicker: TimePickerDialog
    private lateinit var selectedDate: LocalDateTime
    private lateinit var currentDate: LocalDateTime

    private lateinit var dateTimeCover: LinearLayout
    private lateinit var dateTimeInput: TextView
    private lateinit var addressField: TextView
    private lateinit var doneBtn: ConstraintLayout

    private lateinit var dataPref: SharedPreferences
    private var homeAddress: Address? = null
    private var businessAddress: Address? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidThreeTen.init(requireContext());
        initCurrentDate()
        initElements()

        dataPref = requireActivity().getSharedPreferences("cached_data", Context.MODE_PRIVATE)
        initDefaultAddress()
        toggleAddressType(liveOrder.addressType.value?: "home")

        queue = Volley.newRequestQueue(requireContext())
        datePicker = DatePickerDialog(requireContext(), this, selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth)
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        timePicker = TimePickerDialog(requireContext(), this, selectedDate.hour, selectedDate.minute, false)
        dateTimeInput.setOnClickListener {
            datePicker.show()
        }

        locationField.setOnClickListener {
            // load address fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down,R.anim.slide_up, R.anim.slide_down)
                .replace(R.id.addressFrameLayoutFragment, AddressFragment())
                .addToBackStack(null)
                .commit()
        }

        addressSet()
    }

    private fun initDefaultAddress() {
        val homeAddressJson = dataPref.getString(getString(R.string.sh_home_address), null)
        homeAddress = if (homeAddressJson != null) {
            gson.fromJson(homeAddressJson, Address::class.java)
        }else null

        val businessAddressJson = dataPref.getString(getString(R.string.sh_business_address), null)
        businessAddress = if (businessAddressJson != null) {
            gson.fromJson(businessAddressJson, Address::class.java)
        }else null
    }

    private fun initCurrentDate() {
        currentDate = LocalDateTime.now()
        selectedDate = LocalDateTime.now()
    }

    private fun initElements() {
        loadingCover = requireActivity().findViewById(R.id.loadingCoverConstraint)
        loadingAvi = loadingCover.findViewById(R.id.avi)
        errorMsg = requireActivity().findViewById(R.id.errorMsg)

        liveOrder = ViewModelProviders.of(requireActivity()).get(LiveOrder::class.java)

        quantity = requireActivity().findViewById(R.id.quantityInput)
        gasUnit = requireActivity().findViewById(R.id.gasUnit)
        gasUnit.text = liveOrder.gasUnit.value

        homeAddressToggle = requireActivity().findViewById(R.id.homeAddressBtn)
        homeAddressToggle.setOnClickListener{toggleAddressType("home")}

        businessAddressToggle = requireActivity().findViewById(R.id.businessAddressBtn)
        businessAddressToggle.setOnClickListener{toggleAddressType("business")}

        payOnDeliveryToggle = requireActivity().findViewById(R.id.payOnDeliveryBtn)
        payOnDeliveryToggle.setOnClickListener { togglePaymentTime(false) }

        scheduleLaterToggle = requireActivity().findViewById(R.id.scheduleLaterBtn)
        scheduleLaterToggle.setOnClickListener { togglePaymentTime(true) }

        paymentTimeLbl = requireActivity().findViewById(R.id.paymentTimeLbl)
        paymentTimeToggleGroup = requireActivity().findViewById(R.id.paymentSchLayout)

        dateTimeCover = requireActivity().findViewById(R.id.dateTimeContainer)
        dateTimeInput = requireActivity().findViewById(R.id.dateTimeInput)

        addressField = requireActivity().findViewById(R.id.locationField)

        doneBtn = requireActivity().findViewById(R.id.doneBtn)
        doneBtn.setOnClickListener { makeOrderPayment() }
    }

    private fun toggleAddressType(addressType: String) {
        liveOrder.addressType.value = addressType

        if (addressType == "home") {
            homeAddressToggle.isChecked = true
            businessAddressToggle.isChecked = false

            if (homeAddress != null) liveOrder.deliveryAddress.value = homeAddress else {
                Toast.makeText(requireContext(), "No saved home address", Toast.LENGTH_SHORT).show()
            }
        }else {
            homeAddressToggle.isChecked = false
            businessAddressToggle.isChecked = true

            if (businessAddress != null) liveOrder.deliveryAddress.value = businessAddress else {
                Toast.makeText(requireContext(), "No saved business address", Toast.LENGTH_SHORT).show()
            }
        }

        togglePaymentTime(false)
    }

    private fun togglePaymentTime(scheduled: Boolean) {

        if (liveOrder.addressType.value == "home") {
            payOnDeliveryToggle.isChecked = false
            scheduleLaterToggle.isChecked = false

            paymentTimeLbl.visibility = View.GONE
            paymentTimeToggleGroup.visibility = View.GONE
            dateTimeCover.visibility = View.GONE

        }else {
            paymentTimeLbl.visibility = View.VISIBLE
            paymentTimeToggleGroup.visibility = View.VISIBLE

            if (scheduled) {
                payOnDeliveryToggle.isChecked = false
                scheduleLaterToggle.isChecked = true
                dateTimeCover.visibility = View.VISIBLE

            }else {
                payOnDeliveryToggle.isChecked = true
                scheduleLaterToggle.isChecked = false
                dateTimeCover.visibility = View.GONE
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        selectedDate = selectedDate.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth)
        timePicker.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        selectedDate = selectedDate.withHour(hourOfDay).withMinute(minute)

        if (selectedDate > LocalDateTime.now()) {

            dateTimeInput.text = getString(R.string.date_format).format(
                selectedDate.dayOfMonth,
                selectedDate.monthValue,
                selectedDate.year,

                if (selectedDate.hour > 9) selectedDate.hour else "0${selectedDate.hour}",
                if (selectedDate.minute > 9) selectedDate.minute else "0${selectedDate.minute}"
            )

        } else {
            dateTimeInput.text = ""
            Toast.makeText(requireContext(), "Time must be in the future", Toast.LENGTH_LONG).show()
        }
    }

    private fun addressSet() {
        liveOrder.deliveryAddress.observe(viewLifecycleOwner, Observer{address->
            locationField.text = address.address
        })
    }

    private fun makeOrderPayment() {
        try {
            if (quantity.text.toString().isNotEmpty()) {
                val quantity: Double = quantity.text.toString().toDouble()

                if (quantity <= 0) {
                    showError("Quantity must be grater than 0")
                    return
                }

                if (liveOrder.deliveryAddress.value == null) {
                    showError("Select delivery address")
                    return
                }

                //todo regulate business quantity

                liveOrder.quantity.value = quantity
                liveOrder.addressType.value = if (homeAddressToggle.isChecked) "home" else "business"
                if (businessAddressToggle.isChecked && scheduleLaterToggle.isChecked) {
                    liveOrder.scheduledDate.value = selectedDate
                }

                startMakePaymentFragment()
            }else {
                showError("Quantity is required")
            }
        }catch (e: Exception) {
            showError("An error occurred")
            Log.v("ApiLog", "Date ${e.message}")
        }
    }

    private fun startMakePaymentFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down,R.anim.slide_up, R.anim.slide_down)
            .replace(R.id.frameLayoutFragment, PaymentFragment())
            .addToBackStack(null)
            .commit()
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
