package festusyuma.com.glaid.model.live

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.AutocompletePrediction

class LiveAddress:  ViewModel() {
    val place: MutableLiveData<AutocompletePrediction> = MutableLiveData()
}