package ee.ut.cs.iotbazaar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var itemViewModel: ItemViewModel
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

        // Buttons
        binding.Camera.setOnClickListener {
            findNavController().navigate(R.id.action_toCamera)
        }
        binding.button2.setOnClickListener {
            // Navigate directly to the destination fragment id
            findNavController().navigate(R.id.navigation_catalog)
        }
        binding.qrCodeBtn.setOnClickListener {
            findNavController().navigate(R.id.qrCodeScanner)
        }

        // Fetch and display random quote
        fetchRandomQuote()

        return root
    }

    private fun fetchRandomQuote() {
        // Show loading state
        binding.MOTD.text = "Loading quote..."

        RetrofitClient.quoteApiService.getRandomQuote().enqueue(object : Callback<Quote> {
            override fun onResponse(call: Call<Quote>, response: Response<Quote>) {
                if (response.isSuccessful && response.body() != null) {
                    val quote = response.body()!!
                    binding.MOTD.text = "\"${quote.quote}\"\n- ${quote.author}"
                } else {
                    binding.MOTD.text = "Failed to load quote. Please try again later."
                }
            }

            override fun onFailure(call: Call<Quote>, t: Throwable) {
                binding.MOTD.text = "No internet connection. Please check your network."
                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}