package app.blinkshare.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.blinkshare.android.databinding.ActivityMain2Binding
import app.blinkshare.android.utills.AppUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File

class MainActivity2 : AppCompatActivity(), View.OnClickListener  {
    private lateinit var binding: ActivityMain2Binding
    private var lensFacing = CameraX.LensFacing.BACK
    private var flashMode: Boolean = false
    private lateinit var preview: Preview
    private var imageCapture: ImageCapture? = null
    private lateinit var imageCaptureConfig: ImageCaptureConfig
    private lateinit var textureView: TextureView
    private val REQUEST_CODE_PERMISSIONS = 999
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        textureView = binding.textureView
        if (allPermissionsGranted()) {
            binding.textureView.post {
                startCameraForCapture()
            }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }


        binding.tvChats.setOnClickListener(this)
        binding.tvTrending.setOnClickListener(this)
        binding.imgFlash.setOnClickListener(this)
        binding.imgGallery.setOnClickListener(this)
        binding.imgBackBtn.setOnClickListener(this)
        binding.imgCapturePhoto.setOnClickListener(this)
        binding.imgSwitchMode.setOnClickListener(this)
        binding.imgSwitchMode.setOnClickListener(this)
    }

    private fun startCameraForCapture() {
        CameraX.unbindAll()
        // pull the metrics from our TextureView
        val metrics = DisplayMetrics().also { textureView.display.getRealMetrics(it) }
        // define the screen size
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(screenAspectRatio)
            setTargetResolution(screenSize)
            setLensFacing(lensFacing)
        }.build()
        // Build the viewfinder use case
        preview = Preview(previewConfig)
        //preview.enableTorch(false)
        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener { previewOutput: Preview.PreviewOutput ->
            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = textureView.parent as ViewGroup
            parent.removeView(textureView)
            parent.addView(textureView, 0)
            textureView.setSurfaceTexture(previewOutput.surfaceTexture)
            updateTransform()
        }
        imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            setLensFacing(lensFacing)
            setFlashMode(if (flashMode) FlashMode.ON else FlashMode.OFF)
            setTargetRotation(windowManager.defaultDisplay.rotation)
            //setTargetRotation(Surface.ROTATION_0)
        }.build()

        // Build the viewfinder use case
        imageCapture = ImageCapture(imageCaptureConfig)
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }


    /**
     * Handle OnClick events
     */
    override fun onClick(view: View?) {
        when (view) {
            binding.imgBackBtn -> {
                finish()
            }
            binding.imgSwitchMode -> {
                lensFacing = if (CameraX.LensFacing.FRONT == lensFacing) {
                    CameraX.LensFacing.BACK
                } else {
                    CameraX.LensFacing.FRONT
                }
                try {
                    // Only bind use cases if we can query a camera with this orientation
                    //CameraX.getCameraWithLensFacing(lensFacing)
                    startCameraForCapture()
                } catch (exc: Exception) {
                    // Do nothing
                }
            }
            binding.imgFlash -> {
                if (flashMode) {
                    flashMode = false
                    binding.imgCloseSmall.visibility = View.VISIBLE
                } else {
                    flashMode = true
                    binding.imgCloseSmall.visibility = View.GONE
                }
                try {
                    startCameraForCapture()
                } catch (exc: Exception) {
                    // Do nothing
                }
            }
            binding.imgCapturePhoto -> {
                binding.rlProgressLoading.visibility = View.VISIBLE
                val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
                imageCapture?.takePicture(file, object : ImageCapture.OnImageSavedListener {
                    override fun onImageSaved(file: File) {
                        binding.rlProgressLoading.visibility = View.GONE
                        val intent = Intent(applicationContext, AddProductActivity::class.java)
                        intent.putExtra("photo", file!!.absolutePath)
                        startActivity(intent)

                    }

                    override fun onError(
                        useCaseError: ImageCapture.UseCaseError,
                        message: String,
                        cause: Throwable?
                    ) {
                        val msg = "Photo capture failed: $message"
                        msg.toast()
                        cause?.printStackTrace()
                    }
                })
            }
            binding.imgGallery -> {
                ImagePicker.with(this)
                    .compress(1024)
                    .galleryOnly()    //User can only select image from Gallery
                    .start()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //You can get File object from intent
            val file: File? = ImagePicker.getFile(data)
            val intent = Intent(this, AddProductActivity::class.java)
            intent.putExtra("photo", file!!.absolutePath)
            startActivity(intent)

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            //Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            // Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = textureView.width / 2f
        val centerY = textureView.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegree = when (textureView.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegree.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        textureView.setTransform(matrix)
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                textureView.post {
                    //startCameraForPreview()
                    startCameraForCapture()
                }
            } else {
                "Permissions not granted by the user.".toast()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun String.toast() {
        Toast.makeText(
            applicationContext,
            this,
            Toast.LENGTH_SHORT
        ).show()
    }




}