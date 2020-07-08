package festusyuma.com.glaid.model.live

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import festusyuma.com.glaid.model.Address
import org.threeten.bp.LocalDateTime

class LiveOrder: ViewModel() {
    var quantity: MutableLiveData<Double> = MutableLiveData()
    var addressType: MutableLiveData<String> = MutableLiveData()
    var gasTypeId: MutableLiveData<Long> = MutableLiveData()
    var gasUnit: MutableLiveData<String> = MutableLiveData()
    var deliveryAddress: MutableLiveData<Address> = MutableLiveData()
    var paymentType: MutableLiveData<String> = MutableLiveData()
    var paymentCardId: MutableLiveData<Long> = MutableLiveData()
    var scheduledDate: MutableLiveData<LocalDateTime> = MutableLiveData()
}