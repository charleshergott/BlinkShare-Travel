package app.blinkshare.android.notification

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAwUrP_YQ:APA91bHND1ZIm7moILQ0lMKdLQr0g5FejvexYucsuZM6cgVfrbrPuY1f_h_1CpRG7iwsDz-ljnsRJFI_DZ-CGopSqMWnoRdn2qpd8vI04a3KRQzLh8DkmqZxt3mDKhwRxWX0fESfqYy7"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): Call<MyResponse?>?
}