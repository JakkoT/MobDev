package ee.ut.cs.iotbazaar.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.databinding.FragmentHomeBinding
import ee.ut.cs.iotbazaar.ui.item.ItemAdapter
import ee.ut.cs.iotbazaar.ui.item.ItemViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var itemViewModel: ItemViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val itemAdapter = ItemAdapter { item ->
        // Long press deletes item
        itemViewModel.deleteItem(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        itemViewModel = ViewModelProvider(this).get(ItemViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup reserved items RecyclerView
        binding.reservedItemsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }

        // Observe items
        itemViewModel.items.observe(viewLifecycleOwner) { items ->
            val reservedItems = items.filter { it.reserved } // only reserved
            itemAdapter.submit(reservedItems)
            binding.reservedEmpty.visibility = if (reservedItems.isEmpty()) View.VISIBLE else View.GONE
        }

        // Replace old seeding calls with unified method
        itemViewModel.ensureReservedSeedItems()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())


        // Buttons
        binding.Camera.setOnClickListener {
            findNavController().navigate(R.id.action_toCamera)
        }
        binding.button2.setOnClickListener {
            // Navigate directly to the destination fragment id
            findNavController().navigate(R.id.navigation_catalog)
        }
        binding.qrCodeBtn.setOnClickListener {
            if (hasLocationPermission()) {
                verifyDeltaLocationAndNavigate()
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun verifyDeltaLocationAndNavigate() {
        // Use HIGH_ACCURACY to better force a GPS lock
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location == null) {
                    // CASE 1: Location is null (e.g., indoors, emulator no location)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.delta_location_unavailable),
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                // CASE 2: We have a location. Now we check the distance.
                if (isWithinDelta(location)) {
                    // User is inside the radius
                    navigateToQrScanner()
                } else {
                    // CASE 3: We have a location, but it's too far away.
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.delta_location_required),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener {
                // CASE 4: The location request failed (e.g., user turned off GPS)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.delta_location_unavailable),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun isWithinDelta(userLocation: Location): Boolean {
        val deltaLocation = Location("Delta").apply {
            latitude = DELTA_LATITUDE
            longitude = DELTA_LONGITUDE
        }
        return userLocation.distanceTo(deltaLocation) <= DELTA_RADIUS_METERS
    }

    private fun navigateToQrScanner() {
        findNavController().navigate(R.id.qrCodeScanner)
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
            verifyDeltaLocationAndNavigate()
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            Toast.makeText(
                requireContext(),
                getString(R.string.delta_location_permission_denied),
                Toast.LENGTH_LONG
            ).show()
        }
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