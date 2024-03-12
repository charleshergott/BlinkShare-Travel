package app.blinkshare.android.model

import android.os.Parcel
import android.os.Parcelable

data class User(
    val userName: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val age: String = "",
    val authToken: String = "",
    val avatar: String = "",
    val currency: String = "",
    val location: UserLocation? = null,
    val login: Boolean = false,
    @field:JvmField // use this annotation if your Boolean field is prefixed with 'is'
    val isAdmin: Boolean = false
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readParcelable(UserLocation::class.java.classLoader),
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
        null,
        false,
        false
    ) // this constructor is an explicit
    // "empty" constructor, as seen by Java.
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userName)
        parcel.writeString(email)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(age)
        parcel.writeString(authToken)
        parcel.writeString(avatar)
        parcel.writeString(currency)
        parcel.writeParcelable(location, flags)
        parcel.writeByte(if (login) 1 else 0)
        parcel.writeByte(if (isAdmin) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}