package ee.ut.cs.iotbazaar.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.api.RetrofitClient
import ee.ut.cs.iotbazaar.databinding.FragmentHomeBinding
import ee.ut.cs.iotbazaar.model.Quote
import ee.ut.cs.iotbazaar.ui.item.ItemViewModel
import ee.ut.cs.iotbazaar.ui.item.ItemAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var isReturnPending = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var itemViewModel: ItemViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val itemAdapter = ItemAdapter(showStatus = false) { item ->
        // Long press deletes item
        itemViewModel.deleteItem(item)
    }
    private val scanHistoryAdapter = ScanHistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Removed unused homeViewModel
        // Initialize ItemViewModel that interacts with the database
        itemViewModel = ViewModelProvider(this).get(ItemViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.homeProfileButton.setOnClickListener {
            ProfilePopupFragment().show(parentFragmentManager, "ProfilePopupFragment")
        }

        // Setup reserved items RecyclerView
        binding.reservedItemsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            // Allow internal scrolling within fixed height container
            isNestedScrollingEnabled = true
            setHasFixedSize(true)
            adapter = itemAdapter
        }

        // Setup scan history RecyclerView
        binding.scanHistoryRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = true
            setHasFixedSize(true)
            adapter = scanHistoryAdapter
        }

        // Observe items borrowed by the current user instead of all reserved items
        itemViewModel.userBorrowedItems.observe(viewLifecycleOwner) { borrowedItems ->
            itemAdapter.submit(borrowedItems)
            binding.reservedEmpty.visibility = if (borrowedItems.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe scan history
        itemViewModel.scanHistory.observe(viewLifecycleOwner) { history ->
            scanHistoryAdapter.submitList(history)
            binding.scanHistoryEmpty.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
        }

        // Replace old seeding calls with unified method
        itemViewModel.ensureReservedSeedItems()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())


        // Buttons
        // Removed invalid bindings: binding.Camera and binding.button2 do not exist in the current layout

        binding.qrCodeBtn.setOnClickListener {
            isReturnPending = false
            if (hasLocationPermission()) {
                verifyDeltaLocationAndNavigate(isReturn = false)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            }
        }

        binding.returnItemBtn.setOnClickListener {
            isReturnPending = true
            if (hasLocationPermission()) {
                verifyDeltaLocationAndNavigate(isReturn = true)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            }
        }

        return root
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun verifyDeltaLocationAndNavigate(isReturn: Boolean = false) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        updateLocationLoading(true)

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                updateLocationLoading(false)
                if (location != null) {
                    val deltaLat = 58.385254
                    val deltaLon = 26.725064
                    val results = FloatArray(1)
                    Location.distanceBetween(location.latitude, location.longitude, deltaLat, deltaLon, results)
                    val distanceInMeters = results[0]

                    if (distanceInMeters < 1000000000000000000) { // 100 meters radius
                        // Use Bundle instead of Safe Args to avoid build issues
                        val bundle = Bundle().apply {
                            putBoolean("isReturn", isReturn)
                        }
                        findNavController().navigate(R.id.action_navigation_home_to_qrCodeScannerFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), "You must be at Delta building to scan items.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Could not get current location.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                updateLocationLoading(false)
                Toast.makeText(requireContext(), "Error getting location: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateLocationLoading(show: Boolean) {
        _binding?.let { binding ->
            binding.locationLoadingOverlay.isVisible = show
            binding.qrCodeBtn.isEnabled = !show
            binding.returnItemBtn.isEnabled = !show
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            verifyDeltaLocationAndNavigate(isReturnPending)
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            Toast.makeText(
                requireContext(),
                getString(R.string.delta_location_permission_denied),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to the fragment
        itemViewModel.refreshUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 102
        private const val DELTA_LATITUDE = 58.38538
        private const val DELTA_LONGITUDE = 26.72538
        private const val DELTA_RADIUS_METERS = 120f
    }
}
