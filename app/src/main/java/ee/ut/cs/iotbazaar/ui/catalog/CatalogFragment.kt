package ee.ut.cs.iotbazaar.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ee.ut.cs.iotbazaar.databinding.FragmentCatalogBinding
import ee.ut.cs.iotbazaar.ui.item.ItemAdapter
import ee.ut.cs.iotbazaar.ui.item.ItemViewModel


// CatalogFragment for displaying a list of items in the catalog.
class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemViewModel: ItemViewModel
    private val adapter = ItemAdapter(onLongPressDelete = null) // no delete in catalog for now
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemViewModel = ViewModelProvider(this).get(ItemViewModel::class.java)

        binding.catalogRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.catalogRecycler.adapter = adapter
        // Observe items from ViewModel and submit to adapter
        itemViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submit(items)
        }
        // Ensure items seeded (safe if already populated)
        itemViewModel.seedIfEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
