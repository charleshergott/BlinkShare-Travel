package app.blinkshare.android.utills

import android.content.Context
import android.net.ConnectivityManager
import android.util.Patterns
import app.blinkshare.android.model.User
import app.blinkshare.android.setIsAdmin
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class AppUtils {
    fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    fun isValidUserName(userName: String): Boolean {
        val regex = "^[a-z]\\w{5,29}$"
        val pattern = Pattern.compile(regex)
        return pattern.matcher(userName).matches()
    }

    fun isEulaAgreed(context: Context): Boolean {
        return SharedPrefManager().getInstance(context).getBoolean("EULA", false);
    }

    fun setEulaAgreed(context: Context) {
        SharedPrefManager().getInstance(context).saveBoolean("EULA", true);
    }

    fun isUserLoggedIn(context: Context): Boolean {
        return SharedPrefManager().getInstance(context).getBoolean("loggedIn", false);
    }

    fun setUserLoggedIn(context: Context) {
        SharedPrefManager().getInstance(context).saveBoolean("loggedIn", true);
    }

    fun setUserId(context: Context, userId: String) {
        SharedPrefManager().getInstance(context).saveString("userId", userId);
    }

    fun getUserId(context: Context): String? {
        return SharedPrefManager().getInstance(context).getString("userId","");
    }

    fun logoutUser(context: Context) {
        SharedPrefManager().getInstance(context).saveBoolean("loggedIn", false)
        SharedPrefManager().getInstance(context).saveString("userId", "")
        context.setIsAdmin(false)

    }

    fun saveUser(context: Context, user : User) {
        var userJson = Gson().toJson(user, User::class.java)
        SharedPrefManager().getInstance(context).saveString("user", userJson);
    }

    fun getUser(context: Context) :User?{
        val userJson = SharedPrefManager().getInstance(context).getString("user","");
        if(userJson.isNullOrEmpty())
            return null
        return Gson().fromJson(userJson, User::class.java)
    }



    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    public fun getCurrentDate(): String {
        try {
            val today = Date()
            var format = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val time = format.format(today)
            return time;
        } catch (e: Exception) {
            e.printStackTrace()
            return "";
        }
    }

    public fun getFormattedCurrentDate(): String {
        try {
            val today = Date()
            var format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val time = format.format(today)
            return time;
        } catch (e: Exception) {
            e.printStackTrace()
            return "";
        }
    }
}