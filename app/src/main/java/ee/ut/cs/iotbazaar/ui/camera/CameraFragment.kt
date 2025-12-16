package ee.ut.cs.iotbazaar.ui.camera

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.databinding.CameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Fragment for displaying the camera preview.
 * Handles camera permissions and lifecycle binding for CameraX.
 * Note: This fragment seems to be a basic camera preview, potentially for testing or a different mode than the QR scanner.
 */
class CameraFragment : Fragment() {

    private var _binding: CameraBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CameraBinding.inflate(inflater, container, false)
        return binding.root
    }
    /**
     * Called immediately after onCreateView.
     * Sets up back button handling and starts the camera if permissions are granted.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button  navigates home using the nav action
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Use the action defined in navigation graph
                    findNavController().navigate(R.id.action_camera_to_navigation_home)
                }
            }
        )

        // Start camera when permission granted
        if (hasCameraPermission()) {
            startCamera()
        } else {
            // Request again
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    /**
     * Checks if the app has camera permission.
     *
     * @return True if camera permission is granted, false otherwise.
     */
    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    /**
     * Handles the result of the permission request.
     * If granted, starts the camera; if denied, navigates back to home.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && hasCameraPermission()) {
            startCamera()
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            // Permission denied – navigate back to home for now
            findNavController().navigate(R.id.action_camera_to_navigation_home)
        }
    }

    /**
     * Initializes and starts the camera preview.
     * Binds the camera lifecycle to the fragment's view lifecycle.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also { p ->
                p.surfaceProvider = binding.viewFinder.surfaceProvider
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                // If binding fails, just navigate back (basic fallback)
                findNavController().navigate(R.id.action_camera_to_navigation_home)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }



    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 101
    }
}