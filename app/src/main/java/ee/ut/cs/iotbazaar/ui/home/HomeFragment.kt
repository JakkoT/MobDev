package ee.ut.cs.iotbazaar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.databinding.FragmentHomeBinding
import ee.ut.cs.iotbazaar.ui.item.ItemViewModel
import ee.ut.cs.iotbazaar.ui.item.ItemAdapter

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
            itemAdapter.submit(items)
            binding.reservedEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        // Seed sample items once (safe to call repeatedly)
        itemViewModel.seedIfEmpty()

        // Buttons
        binding.Camera.setOnClickListener {
            findNavController().navigate(R.id.action_toCamera)
        }
        binding.button2.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_catalog)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}