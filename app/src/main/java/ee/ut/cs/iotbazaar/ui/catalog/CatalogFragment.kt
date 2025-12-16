package ee.ut.cs.iotbazaar.ui.catalog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ee.ut.cs.iotbazaar.databinding.FragmentCatalogBinding
import ee.ut.cs.iotbazaar.ui.home.ProfilePopupFragment
import ee.ut.cs.iotbazaar.ui.item.ItemAdapter
import ee.ut.cs.iotbazaar.ui.item.ItemViewModel
import androidx.navigation.fragment.findNavController

/**
 * Fragment responsible for displaying the catalog of items.
 * Allows users to view items and add new items to the inventory.
 */
class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!! // ViewBinding valid only between onCreateView/onDestroyView

    private lateinit var itemViewModel: ItemViewModel
    private val adapter = ItemAdapter(onLongPressDelete = null) // no delete in catalogue view

    // Flag to know if we should scroll after next list emission
    private var pendingScrollToBottom = false

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView.
     * Sets up the RecyclerView, observes ViewModel data, and handles UI interactions.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemViewModel = ViewModelProvider(this).get(ItemViewModel::class.java)

        // RecyclerView setup
        val lm = LinearLayoutManager(requireContext())
        binding.catalogRecycler.layoutManager = lm
        binding.catalogRecycler.adapter = adapter

        // Observe DB items
        itemViewModel.items.observe(viewLifecycleOwner) { items ->
            val oldSize = adapter.itemCount
            adapter.submit(items)
            if (pendingScrollToBottom && items.isNotEmpty() && items.size > oldSize) {
                binding.catalogRecycler.post {
                    binding.catalogRecycler.scrollToPosition(items.size - 1)
                }
            }
            pendingScrollToBottom = false
        }
        itemViewModel.ensureReservedSeedItems()

        binding.catalogProfileButton.setOnClickListener {
            ProfilePopupFragment().show(parentFragmentManager, "ProfilePopupFragment")
        }

        binding.catalogBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Text change validation
        binding.itemNameInput.doOnTextChanged { text, _, _, _ ->
            validateForm(text?.toString())
        }

        // Add button logic
        binding.addItemButton.setOnClickListener {
            val name = binding.itemNameInput.text?.toString()?.trim().orEmpty()
            val quantityStr = binding.itemQuantityInput.text?.toString()?.trim().orEmpty()
            val quantity = quantityStr.toIntOrNull() ?: 1

            if (name.isEmpty()) {
                binding.itemNameLayout.error = "Name required"
                binding.addItemButton.isEnabled = false
            } else {
                binding.itemNameLayout.error = null
                pendingScrollToBottom = true
                itemViewModel.addItem(name, quantity)
                hideKeyboard()
                android.widget.Toast.makeText(requireContext(), "Item $name added successfully", android.widget.Toast.LENGTH_LONG).show()
                // Reset form
                binding.itemNameInput.setText("")
                binding.itemQuantityInput.setText("")
                binding.addItemButton.isEnabled = false
            }
        }
    }

    /**
     * Validates the item name input field.
     * Enables or disables the add button based on input validity.
     *
     * @param raw The raw text input from the user.
     */
    private fun validateForm(raw: String?) {
        val name = raw?.trim().orEmpty()
        if (name.isEmpty()) {
            binding.itemNameLayout.error = "Name required"
            binding.addItemButton.isEnabled = false
        } else {
            binding.itemNameLayout.error = null
            binding.addItemButton.isEnabled = true
        }
    }

    /**
     * Hides the soft keyboard.
     */
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.itemNameInput.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
