package festusyuma.com.glaid

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {
    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    private var _context: Context = context

    // shared pref mode
    var PRIVATE_MODE = 0
    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
        editor.commit()
    }

    fun isFirstTimeLaunch(): Boolean {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
    }

    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "welcome"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}