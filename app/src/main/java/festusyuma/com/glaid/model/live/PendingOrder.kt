package festusyuma.com.glaid.model.live

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import festusyuma.com.glaid.model.Truck

class PendingOrder: ViewModel() {
    val gasType: MutableLiveData<String> = MutableLiveData()
    val gasUnit: MutableLiveData<String> = MutableLiveData()
    val quantity: MutableLiveData<Double> = MutableLiveData()
    val amount: MutableLiveData<Double> = MutableLiveData()
    var statusId: MutableLiveData<Long> = MutableLiveData()
    var truck: MutableLiveData<Truck> = MutableLiveData()
    var driverName: MutableLiveData<String> = MutableLiveData()
}