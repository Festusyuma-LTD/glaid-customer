package festusyuma.com.glaid.model.live

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveAddress:  ViewModel() {
    val placeId: MutableLiveData<String> = MutableLiveData()
}