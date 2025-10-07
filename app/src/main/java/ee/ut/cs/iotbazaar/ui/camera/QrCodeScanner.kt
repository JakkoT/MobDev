package ee.ut.cs.iotbazaar.ui.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import ee.ut.cs.iotbazaar.databinding.FragmentQrCodeScannerBinding

class QrCodeScannerFragment : Fragment() {

    private var _binding: FragmentQrCodeScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var scanner: GmsBarcodeScanner
    private var isScannerInstalled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrCodeScannerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // System bar padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button navigates back in NavController
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )

        // Install Google QR Scanner and start scanning immediately
        installGoogleScanner()
    }

    private fun installGoogleScanner() {
        val context = requireContext()
        val moduleInstall = ModuleInstall.getClient(context)
        val moduleInstallRequest = ModuleInstallRequest.newBuilder()
            .addApi(GmsBarcodeScanning.getClient(context))
            .build()

        moduleInstall.installModules(moduleInstallRequest)
            .addOnSuccessListener {
                isScannerInstalled = true
                initScanner()
                startScanning()
            }
            .addOnFailureListener {
                isScannerInstalled = false
                Toast.makeText(context, "Skanneri install ebaõnnestus: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initScanner() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        scanner = GmsBarcodeScanning.getClient(requireContext(), options)
    }

    private fun startScanning() {
        if (!isScannerInstalled) {
            Toast.makeText(requireContext(), "Moodul pole valmis, proovi uuesti", Toast.LENGTH_SHORT).show()
            return
        }

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val result = barcode.rawValue ?: "Tühi väärtus"
                binding.scannedValueTv.text = "Skaneeritud väärtus:\n$result"
                Toast.makeText(requireContext(), "Skaneeritud: $result", Toast.LENGTH_SHORT).show()
            }
            .addOnCanceledListener {
                Toast.makeText(requireContext(), "Tühistatud", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Viga: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

