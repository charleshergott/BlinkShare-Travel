package app.blinkshare.android

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import app.blinkshare.android.databinding.NetworkPopUpBinding
import app.blinkshare.android.utills.ScreenSizeCompat

class NetworkPopUp : DialogFragment() {
    private lateinit var binding: NetworkPopUpBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = NetworkPopUpBinding.inflate(inflater, container, false)
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.tvOk.setOnClickListener {
            dismiss()
        }

    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = ScreenSizeCompat.getScreenSize(requireContext())
        window?.setLayout((size.width * 0.75).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        super.onResume()
    }

}