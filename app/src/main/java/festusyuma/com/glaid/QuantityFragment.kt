package festusyuma.com.glaid

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import festusyuma.com.glaid.model.CustomDate
import festusyuma.com.glaid.model.Order
import festusyuma.com.glaid.model.live.LiveOrder
import kotlinx.android.synthetic.main.fragment_quantity.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class QuantityFragment : Fragment(R.layout.fragment_quantity), DatePickerDialog.OnDateSetListener,  TimePickerDialog.OnTimeSetListener{

    private val order = Order(addressType = "home", gasTypeId = 1)
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
    private lateinit var selectedDate: Calendar
    private lateinit var currentDate: Calendar

    private lateinit var dateTimeCover: LinearLayout
    private lateinit var dateTimeInput: TextView
    private lateinit var addressField: TextView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initCurrentDate()
        initElements()
        toggleAddressType(order.addressType?: "home")

        datePicker = DatePickerDialog(
            requireContext(),
            this,
            selectedDate[Calendar.YEAR],
            selectedDate[Calendar.MONTH],
            selectedDate[Calendar.DAY_OF_MONTH]
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

        timePicker = TimePickerDialog(
            requireContext(),
            this,
            selectedDate[Calendar.HOUR_OF_DAY],
            selectedDate[Calendar.MINUTE],
            false
        )

        dateTimeInput.setOnClickListener {
            datePicker.show()
        }

        locationField.setOnClickListener {
            // load address fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down,R.anim.slide_up, R.anim.slide_down)
                .add(R.id.addressFrameLayoutFragment, AddressFragment())
                .addToBackStack(null)
                .commit()
        }

        addressSet()
    }

    private fun initCurrentDate() {
        currentDate = Calendar.getInstance()
        selectedDate = Calendar.getInstance()
    }

    private fun initElements() {
        liveOrder = ViewModelProviders.of(requireActivity()).get(LiveOrder::class.java)

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
    }

    private fun toggleAddressType(addressType: String) {
        order.addressType = addressType

        if (addressType == "home") {
            homeAddressToggle.isChecked = true
            businessAddressToggle.isChecked = false
        }else {
            homeAddressToggle.isChecked = false
            businessAddressToggle.isChecked = true
        }

        togglePaymentTime(false)
    }

    private fun togglePaymentTime(scheduled: Boolean) {

        if (order.addressType == "home") {
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
        selectedDate[Calendar.YEAR] = year
        selectedDate[Calendar.MONTH] = month
        selectedDate[Calendar.DAY_OF_MONTH] = dayOfMonth
        timePicker.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        selectedDate[Calendar.HOUR_OF_DAY] = hourOfDay
        selectedDate[Calendar.MINUTE] = minute

        if (selectedDate > currentDate) {

            dateTimeInput.text = getString(R.string.date_format).format(
                selectedDate[Calendar.DAY_OF_MONTH],
                selectedDate[Calendar.MONTH] + 1,
                selectedDate[Calendar.YEAR],

                if (selectedDate[Calendar.HOUR_OF_DAY] > 9) selectedDate[Calendar.HOUR_OF_DAY] else "0${selectedDate[Calendar.HOUR_OF_DAY]}",
                if (selectedDate[Calendar.MINUTE] > 9) selectedDate[Calendar.MINUTE] else "0${selectedDate[Calendar.MINUTE]}"
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
}
