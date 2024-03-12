package app.blinkshare.android

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment
import app.blinkshare.android.databinding.FragmentBlockProductBinding
import app.blinkshare.android.utills.ScreenSizeCompat


class BlockProductFragment(private val listener: OnItemClickListener) : DialogFragment() {

    private lateinit var binding: FragmentBlockProductBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = FragmentBlockProductBinding.inflate(inflater, container,false)
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.tvCancel.setOnClickListener {
            listener.onCancelClick()
            dismiss()
        }
        binding.tvReport.setOnClickListener {
            listener.onReportClick(binding.etReason.text.toString())
            dismiss()
        }
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = ScreenSizeCompat.getScreenSize(requireContext())
        window?.setLayout((size.width * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        super.onResume()
    }

    interface OnItemClickListener{
        fun onReportClick(text: String)
        fun onCancelClick()
    }
}