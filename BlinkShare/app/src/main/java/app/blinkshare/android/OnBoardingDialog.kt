package app.blinkshare.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import app.blinkshare.android.databinding.OnBoardingDialogBinding
import app.blinkshare.android.utills.ScreenSizeCompat


class OnBoardingDialog: DialogFragment() {

    private lateinit var binding: OnBoardingDialogBinding
    private var boardingCount = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = OnBoardingDialogBinding.inflate(inflater, container, false)
        initListeners()
        showDescription()
        return binding.root
    }

    private fun initListeners(){
        binding.tvNext.setOnClickListener {
            if(binding.tvNext.text == "Finish"){
                requireContext().setOnBoarding(false)
                dismiss()
            }
            else {
                boardingCount++
                showDescription()
            }
        }
        binding.tvPrevious.setOnClickListener {
            boardingCount--
            showDescription()
        }
        binding.ivCross.setOnClickListener {
            requireContext().setOnBoarding(false)
            dismiss()
        }

        binding.tvSendFeedback.setOnClickListener {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "charles@brics-consulting.com", null
                )
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Body")
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
            showDescription()
        }
    }

    private fun showDescription(){
        when (boardingCount) {
            0 -> {
                binding.tvPrevious.isEnabled = false
                binding.tvNext.isEnabled = true
                binding.tvCount.text = "1/6"
                binding.tvDescription.text = resources.getString(R.string.on_boarding_1)
                binding.tvPrevious.visibility = View.GONE
                binding.tvSendFeedback.visibility = View.GONE
            }
            1 -> {
                binding.tvPrevious.isEnabled = true
                binding.tvNext.isEnabled = true
                binding.tvCount.text = "2/6"
                binding.tvDescription.text = resources.getString(R.string.on_boarding_2)
                binding.tvPrevious.visibility = View.VISIBLE
                binding.tvSendFeedback.visibility = View.GONE
            }
            2 -> {
                binding.tvPrevious.isEnabled = true
                binding.tvNext.isEnabled = true
                binding.tvCount.text = "3/6"
                binding.tvDescription.text = resources.getString(R.string.on_boarding_3)
                binding.tvNext.text = "Next"
                binding.tvSendFeedback.visibility = View.GONE
            }
            3 -> {
                binding.tvPrevious.isEnabled = true
                binding.tvNext.isEnabled = true
                binding.tvCount.text = "4/6"
                binding.tvDescription.text = resources.getString(R.string.on_boarding_4)
                binding.tvNext.text = "Next"
                binding.tvSendFeedback.visibility = View.GONE
            }
            4 -> {
                binding.tvPrevious.isEnabled = true
                binding.tvNext.isEnabled = true
                binding.tvCount.text = "5/6"
                binding.tvDescription.text = resources.getString(R.string.on_boarding_5)
                binding.tvNext.text = "Next"
                binding.tvSendFeedback.visibility = View.GONE
            }
            5 -> {
                binding.tvPrevious.isEnabled = true
                binding.tvNext.isEnabled = true
                binding.tvCount.text = "6/6"
                binding.tvDescription.text = resources.getString(R.string.on_boarding_6)
                binding.tvNext.text = "Finish"
                binding.tvSendFeedback.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = ScreenSizeCompat.getScreenSize(requireContext())
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
//        val display: Display = window!!.windowManager.defaultDisplay
//        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window?.setLayout((size.width * 0.75).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(binding.checkBox.isChecked || binding.checkBox.isSelected){
            requireContext().setOnBoarding(false)
        }
    }

}