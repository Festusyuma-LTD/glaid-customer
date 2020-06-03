package festusyuma.com.glaid.helpers

import android.content.Context
import android.util.Log
import org.json.JSONObject

class Dashboard {

    companion object {
        fun store(context: Context, data: JSONObject) {
            Log.v("ApiLog", "Response lass: $data")

            val sharedPref = context.getSharedPreferences("cached_data", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear()
                commit()
            }
        }
    }
}