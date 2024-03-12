package app.blinkshare.android

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import app.blinkshare.android.databinding.VerificationPopupBinding
import app.blinkshare.android.utills.ScreenSizeCompat

class VerificationPopup(private val listener: OnItemClickListener, private val isFromSignUp: Boolean = false) : DialogFragment() {
    private lateinit var binding: VerificationPopupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = VerificationPopupBinding.inflate(inflater, container, false)
        initViews()
        initListeners()
        isCancelable = false
        return binding.root
    }

    private fun initViews(){
        if(isFromSignUp){
            binding.tvResend.visibility = View.GONE
        }
        else{
            binding.tvResend.visibility = View.VISIBLE
        }
    }

    private fun initListeners(){
        binding.tvOk.setOnClickListener {
            dismiss()
            listener.onCloseClick()
        }
        binding.tvResend.setOnClickListener {
            dismiss()
            listener.onResendClick()
        }
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = ScreenSizeCompat.getScreenSize(requireContext())
        window?.setLayout((size.width * 0.75).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        listener.onCloseClick()
    }

    interface OnItemClickListener{
        fun onResendClick()
        fun onCloseClick()
    }
}