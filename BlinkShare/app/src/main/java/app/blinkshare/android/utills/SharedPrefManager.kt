package app.blinkshare.android.utills

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SharedPrefManager {
    private var instance: SharedPrefManager? = null
    private var sharedPreferences: SharedPreferences? = null
    private var context : Context? = null ;


    fun getInstance(context: Context): SharedPrefManager {
        this@SharedPrefManager.context = context
        if (instance == null) {
            instance = this@SharedPrefManager;
            //sharedPreferences = context.applicationContext.getSharedPreferences("app_shared_pref",0)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        }
        return instance as SharedPrefManager

    }

    fun saveString(key: String, value: String) {
        val editor = sharedPreferences!!.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun saveBoolean(key: String, value: Boolean?) {
        val editor = sharedPreferences!!.edit()
        editor.putBoolean(key, value!!)
        editor.apply()
    }

    fun getBoolean(key: String, defVal: Boolean?): Boolean {

        return sharedPreferences!!.getBoolean(key, defVal!!)
    }

    fun getString(key: String, dval: String): String? {
        return sharedPreferences!!.getString(key, dval)
    }


    fun removeData(key: String) {
        val editor = sharedPreferences!!.edit()
        editor.remove(key)
        editor.apply()
        editor.commit()

    }


    fun saveInt(key: String, value: Int) {

        val editor = sharedPreferences!!.edit()
        editor.putInt(key, value)
        editor.commit()

    }

    fun getInt(key: String, defVal: Int): Int {

        return sharedPreferences!!.getInt(key, defVal)
    }
}