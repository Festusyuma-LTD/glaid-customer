package festusyuma.com.glaid

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import festusyuma.com.glaid.model.CustomDate
import festusyuma.com.glaid.model.Order
import kotlinx.android.synthetic.main.fragment_quantity.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class QuantityFragment : Fragment(R.layout.fragment_quantity), DatePickerDialog.OnDateSetListener,  TimePickerDialog.OnTimeSetListener{
    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0
    var savedHour = 0
    var savedMinute = 0

    private val order = Order(addressType = "home", gasTypeId = 1)
    private lateinit var homeAddressToggle: ToggleButton
    private lateinit var businessAddressToggle: ToggleButton
    private lateinit var payOnDeliveryToggle: ToggleButton
    private lateinit var scheduleLaterToggle: ToggleButton

    private lateinit var paymentTimeLbl: TextView
    private lateinit var paymentTimeToggleGroup: LinearLayout

    private lateinit var datePicker: DatePickerDialog
    private lateinit var timePicker: TimePickerDialog
    private var selectedDate: CustomDate = CustomDate()

    private lateinit var dateTimeInput: TextView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initElements()
        toggleAddressType(order.addressType?: "home")
        initCurrentDate()

        datePicker = DatePickerDialog(requireContext(), this, selectedDate.year, selectedDate.month, selectedDate.day)
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

        timePicker = TimePickerDialog(requireContext(), this, selectedDate.hour, selectedDate.minute, false)

        dateTimeInput.setOnClickListener {
            datePicker.show()
        }

        LocationField.setOnClickListener {
            // load address fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down,R.anim.slide_up, R.anim.slide_down)
                .replace(R.id.addressFrameLayoutFragment, AddressFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun initCurrentDate() {
        val currentDate = Calendar.getInstance()
        selectedDate.day = currentDate.get(Calendar.DAY_OF_MONTH)
        selectedDate.month = currentDate.get(Calendar.MONTH)
        selectedDate.year = currentDate.get(Calendar.YEAR)
        selectedDate.hour = currentDate.get(Calendar.HOUR_OF_DAY)
        selectedDate.minute = currentDate.get(Calendar.MINUTE)

        Log.v("ApiLog", "${currentDate.get(Calendar.DAY_OF_MONTH)}")
    }

    private fun initElements() {
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

        dateTimeInput = requireActivity().findViewById(R.id.dateTimeContainer)
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
        }else {
            paymentTimeLbl.visibility = View.VISIBLE
            paymentTimeToggleGroup.visibility = View.VISIBLE

            if (scheduled) {
                payOnDeliveryToggle.isChecked = false
                scheduleLaterToggle.isChecked = true
            }else {
                payOnDeliveryToggle.isChecked = true
                scheduleLaterToggle.isChecked = false
            }
        }
    }

    private fun getDateTimeCalender() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
        minute = cal.get(Calendar.MINUTE)

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        selectedDate.year = year
        selectedDate.month = month
        selectedDate.day = dayOfMonth
        timePicker.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

        val selected = Calendar.getInstance()
        val current = Calendar.getInstance()
        selected[Calendar.HOUR_OF_DAY] = hourOfDay
        selected[Calendar.MINUTE] = minute

        if (selected.timeInMillis >= current.timeInMillis) {
            selectedDate.hour = hourOfDay
            selectedDate.minute = minute

            dateTimeInput.text = getString(R.string.date_format).format(
                selectedDate.day,
                selectedDate.month + 1,
                selectedDate.year,
                if (selectedDate.hour > 9) selectedDate.hour else "0${selectedDate.hour}",
                if (selectedDate.minute > 9) selectedDate.minute else "0${selectedDate.minute}"
            )

        } else {
            timePicker.show()
            Toast.makeText(requireContext(), "Time must be in the future", Toast.LENGTH_LONG).show()
        }
    }



}
