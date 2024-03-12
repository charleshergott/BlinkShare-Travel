package app.blinkshare.android.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import app.blinkshare.android.R
import app.blinkshare.android.databinding.ItemObjectBinding
import app.blinkshare.android.databinding.ItemProductBinding
import app.blinkshare.android.model.Product
import app.blinkshare.android.toUTCTime
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.ocpsoft.prettytime.PrettyTime
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.util.*
import kotlin.math.abs

const val Flight = 0
const val Image = 1

class ProductAdapter(
    private val list: List<Product>,
    private val userId: String,
    private val mListener: OnItemClickListeners
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var context: Context
    private val format: NumberFormat = DecimalFormat("00")
    private var storageRef: StorageReference = FirebaseStorage.getInstance().reference

    inner class ViewHolder(val binding: ItemProductBinding, val listener: OnItemClickListeners) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {
            if (!list[position].is_home) {
                val imagesRef = storageRef.child(list[position].image!!)
                imagesRef.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        Glide.with(context).load(downloadUri).into(binding.imgPhoto)
                    }
                }
            } else {
                binding.cvProfilePic.visibility = View.GONE
                binding.llLikeDislike.visibility = View.GONE
            }

            binding.tvDescription.text = list[position].description
            binding.tvDate.visibility = View.VISIBLE
            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    try {
                        if (position < list.size) {
                            val date1 = Date(list[position].createdDateTime).toUTCTime()
                            val date2 = Date().toUTCTime()
                            val diff: Long = date1?.time!! - date2?.time!!
                            var seconds = diff / 1000
                            var minutes = seconds / 60
                            var hours = minutes / 60
                            minutes %= 60
                            seconds %= 60
                            if (hours < 0) {
                                hours = 0
                            }
                            if (minutes < 0) {
                                minutes = 0
                            }
                            if (seconds < 0) {
                                seconds = 0
                            }
                            //val days = hours / 24
//                            val p = PrettyTime()
                            binding.tvDate.text =
                                "${format.format(hours)}:${format.format(minutes % 60)}:${
                                    format.format(seconds % 60)
                                }"
                            if (diff > 0) {
                                handler.postDelayed(this, 1000)
                            }
                        }
                    } catch (ex: Exception) {

                    }
                }
            }
            if (binding.tvDate.text.toString() == "") {
                handler.postDelayed(runnable, 1000)
            }


            if (userId == "") {
                binding.imgLikes.visibility = View.GONE
                binding.imgDisLikes.visibility = View.GONE
                binding.ivReport.visibility = View.GONE
                binding.ivDelete.visibility = View.GONE
            } else {
                binding.imgLikes.visibility = View.VISIBLE
                binding.imgDisLikes.visibility = View.VISIBLE
            }
            if (userId == list[position].authToken) {
                binding.ivDelete.visibility = View.VISIBLE
                binding.ivReport.visibility = View.GONE
                binding.ivDelete.setOnClickListener {
                    handler.removeCallbacks(runnable)
                    listener.onDeleteClick(position)
                }
            } else {
                binding.ivDelete.visibility = View.GONE
                binding.ivReport.visibility = View.VISIBLE
            }
            if (list[position].is_like) {
                binding.imgLikes.imageTintList
                binding.imgLikes.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimary
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.imgLikes.setColorFilter(
                    ContextCompat.getColor(context, R.color.colorWhite),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            if (list[position].is_dis_like) {
                binding.imgDisLikes.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimary
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.imgDisLikes.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.colorWhite
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            binding.imgLikes.setOnClickListener {
                listener.onLikeClick(position)
            }
            binding.imgDisLikes.setOnClickListener {
                listener.onDisLikeClick(position)
            }
            binding.ivReport.setOnClickListener {
                listener.onReportClick(position)
            }
        }

    }

    inner class ViewHolderObject(
        val binding: ItemObjectBinding,
        val listener: OnItemClickListeners
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {
            binding.tvDescription.text = list[position].description
            binding.tvDate.visibility = View.VISIBLE
            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    try {
                        if (position < list.size) {
                            val date1 = Date(list[position].createdDateTime).toUTCTime()
                            val date2 = Date().toUTCTime()
                            val diff: Long = date1?.time!! - date2?.time!!
                            var seconds = diff / 1000
                            var minutes = seconds / 60
                            var hours = minutes / 60
                            minutes %= 60
                            seconds %= 60
                            if (hours < 0) {
                                hours = 0
                            }
                            if (minutes < 0) {
                                minutes = 0
                            }
                            if (seconds < 0) {
                                seconds = 0
                            }
                            //val days = hours / 24
//                            val p = PrettyTime()
                            binding.tvDate.text =
                                "${format.format(hours)}:${format.format(minutes % 60)}:${
                                    format.format(seconds % 60)
                                }"
                            if (diff > 0) {
                                handler.postDelayed(this, 1000)
                            }
                        }
                    } catch (ex: Exception) {

                    }
                }
            }
            if (binding.tvDate.text.toString() == "") {
                handler.postDelayed(runnable, 1000)
            }

            if (userId == "") {
                binding.ivReport.visibility = View.GONE
                binding.ivDelete.visibility = View.GONE
                binding.ivEdit.visibility = View.GONE
            }
            if (!userId.isNullOrEmpty() && userId == list[position].authToken) {
                binding.ivDelete.visibility = View.VISIBLE
                binding.ivEdit.visibility = View.VISIBLE
                binding.ivReport.visibility = View.GONE
                binding.ivDelete.setOnClickListener {
                    listener.onDeleteClick(position)
                }
                binding.ivEdit.setOnClickListener {
                    listener.onEditClick(position)
                }
            } else if(!userId.isNullOrEmpty()) {
                binding.ivDelete.visibility = View.GONE
                binding.ivReport.visibility = View.VISIBLE
                binding.tvSubscribe.visibility = View.VISIBLE
                if(list[position].isSubscribed){
                    binding.tvSubscribe.text = "UnSubscribe"
                }
                else{
                    binding.tvSubscribe.text = "Subscribe"
                }
            }

            binding.flightNote.visibility = View.VISIBLE
            binding.tvDepartureDate.text = "Departure date: " + list[position].departure_date
            binding.tvDepartureTime.text = "Time of Departure: " + list[position].time_of_departure
            binding.tvSeats.text = "No of Seats: " + list[position].number_of_seats
            binding.tvDestination.text = "Destination: " + list[position].destination
            binding.tvPrice.text = "Price: €" + list[position].price
            binding.tvCallUsOn.text =
                "To book, call us on " + if (list[position].call_us_on.isNullOrEmpty()) {
                    "+41763718903"
                } else {
                    list[position].call_us_on
                }
            binding.ivReport.setOnClickListener {
                listener.onReportClick(position)
            }
            binding.tvSubscribe.setOnClickListener {
                listener.onSubscribeClick(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        if (viewType == Flight) {
            val binding: ItemObjectBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.item_object,
                parent, false
            )
            return ViewHolderObject(binding, mListener)
        } else {
            val binding: ItemProductBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.item_product,
                parent, false
            )
            return ViewHolder(binding, mListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderObject -> {
                holder.onBind(position)
            }
            is ViewHolder -> {
                holder.onBind(position)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        if (list[position].is_flight) {
            return Flight
        } else {
            return Image
        }
    }

    interface OnItemClickListeners {
        fun onLikeClick(position: Int)
        fun onDisLikeClick(position: Int)
        fun onDeleteClick(position: Int)
        fun onReportClick(position: Int)
        fun onEditClick(position: Int)
        fun onSubscribeClick(position: Int)
    }
}