package festusyuma.com.glaid.model.live

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import festusyuma.com.glaid.model.Truck
import festusyuma.com.glaid.model.User
import festusyuma.com.glaid.model.fs.FSUser

class PendingOrder: ViewModel() {
    val id: MutableLiveData<Long> = MutableLiveData()
    val gasType: MutableLiveData<String> = MutableLiveData()
    val gasUnit: MutableLiveData<String> = MutableLiveData()
    val quantity: MutableLiveData<Double> = MutableLiveData()
    val amount: MutableLiveData<Double> = MutableLiveData()
    var statusId: MutableLiveData<Long> = MutableLiveData()
    var truck: MutableLiveData<Truck> = MutableLiveData()
    val driver: MutableLiveData<User> = MutableLiveData()
}