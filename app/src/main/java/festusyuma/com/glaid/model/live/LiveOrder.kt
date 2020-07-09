package festusyuma.com.glaid.model.live

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import festusyuma.com.glaid.model.Address
import festusyuma.com.glaid.model.GasType
import org.threeten.bp.LocalDateTime

class LiveOrder: ViewModel() {
    var quantity: MutableLiveData<Double> = MutableLiveData()
    var addressType: MutableLiveData<String> = MutableLiveData()
    var gasType: MutableLiveData<GasType> = MutableLiveData()
    var deliveryAddress: MutableLiveData<Address> = MutableLiveData()
    var paymentType: MutableLiveData<String> = MutableLiveData()
    var paymentCardId: MutableLiveData<Long> = MutableLiveData()
    var scheduledDate: MutableLiveData<LocalDateTime> = MutableLiveData()
}