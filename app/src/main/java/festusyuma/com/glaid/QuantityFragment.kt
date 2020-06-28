package festusyuma.com.glaid

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.widget.DatePicker
import android.widget.TimePicker
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
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        onCustombtnclicked()
        LocationField.setOnClickListener {
            // load address fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down,R.anim.slide_up, R.anim.slide_down)
                .replace(R.id.addressFrameLayoutFragment, AddressFragment.addressInstance())
                .addToBackStack(null)
                .commit()
        }
        dateTimeContainer.setOnClickListener {
            getDateTimeCalender()

            context?.let { it1 -> DatePickerDialog(it1, this, year, month, day).show() }
        }
    }
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
//        val lp: FrameLayout.LayoutParams = FrameLayout.LayoutParams(0, 0)
//        framelayoutFragment.layoutParams = lp
//        print("::::::${framelayoutFragment.height}:::::")
//    }

    companion object {
        fun quantityInstance() = QuantityFragment()
    }

    private fun getDateTimeCalender() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
        minute = cal.get(Calendar.MINUTE)

    }
    private fun pickDate() {

    }
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        getDateTimeCalender()

        TimePickerDialog(context, this, hour, minute, false).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute

        dateTimeField.text = "$savedDay - $savedMonth - $savedYear : $savedHour-$savedMinute"
    }

//    fun onCustombtnclicked() {
//        framelayoutFragment?.setPadding(0,0,0,0)
//    }

}
