package app.blinkshare.android.model

import android.os.Parcel
import android.os.Parcelable

data class Product(
    val id: String,
    val authToken: String,
    val title: String,
    val currency: String,
    val price: String,
    var latitude: String,
    var longitude: String,
    val address: String,
    val description: String,
    var image: String?,
    val status: String,
    var createdDateTime: Long,
    val user: User?,
    var is_like:Boolean,
    var is_dis_like:Boolean,
    var total_like:String,
    var total_dislike:String,
    var blocked_by_user_id:String,
    var flight_time:String?,
    var destination:String?,
    var number_of_seats:String?,
    var time_of_departure:String?,
    var departure_date:String?,
    var call_us_on:String?,
    @field:JvmField // use this annotation if your Boolean field is prefixed with 'is'
    var is_object: Boolean = false,
    @field:JvmField // use this annotation if your Boolean field is prefixed with 'is'
    var is_flight: Boolean = false,
    @field:JvmField // use this annotation if your Boolean field is prefixed with 'is'
    var is_home: Boolean = false,
    var isSubscribed: Boolean = false
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readLong(),
        parcel.readParcelable(User::class.java.classLoader),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()

    ) {
    }

    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        null,
        false,
        false,
        "",
        "",
        "",
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        false,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(authToken)
        parcel.writeString(title)
        parcel.writeString(currency)
        parcel.writeString(price)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(address)
        parcel.writeString(description)
        parcel.writeString(image)
        parcel.writeString(status)
        parcel.writeLong(createdDateTime)
        parcel.writeParcelable(user, flags)
        parcel.writeByte(if (is_like) 1 else 0)
        parcel.writeByte(if (is_dis_like) 1 else 0)
        parcel.writeString(total_like)
        parcel.writeString(total_dislike)
        parcel.writeString(blocked_by_user_id)
        parcel.writeString(flight_time)
        parcel.writeString(destination)
        parcel.writeString(number_of_seats)
        parcel.writeString(time_of_departure)
        parcel.writeString(departure_date)
        parcel.writeString(call_us_on)
        parcel.writeByte(if (is_object) 1 else 0)
        parcel.writeByte(if (is_flight) 1 else 0)
        parcel.writeByte(if (is_home) 1 else 0)
        parcel.writeByte(if (isSubscribed) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}