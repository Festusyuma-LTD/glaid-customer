package festusyuma.com.glaid.model.live

import android.location.Address
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveAddress:  ViewModel() {
    val placeId: MutableLiveData<String> = MutableLiveData()
}