package ee.ut.cs.iotbazaar.ui.item

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.databinding.RowReservedItemBinding
import ee.ut.cs.iotbazaar.model.Item

/**
 * RecyclerView Adapter for displaying items in a list.
 * Supports two modes:
 * 1. Catalog mode (showStatus = true): Displays stock status and availability.
 * 2. Borrowed items mode (showStatus = false): Displays item ID and return date.
 *
 * @param showStatus Boolean flag to toggle between catalog view and borrowed items view.
 * @param onLongPressDelete Optional callback for handling long-press events (e.g., for deletion).
 */
class ItemAdapter(
    private val showStatus: Boolean = true,
    private val onLongPressDelete: ((Item) -> Unit)? = null
) : RecyclerView.Adapter<ItemAdapter.ItemVH>() {

    private val data = mutableListOf<Item>()

    /**
     * Updates the list of items and notifies the adapter.
     * @param list The new list of items to display.
     */
    fun submit(list: List<Item>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * ViewHolder class for binding item data to the view.
     */
    inner class ItemVH(private val binding: RowReservedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Binds an Item object to the view elements.
         * Configures visibility and text based on the adapter's mode (showStatus).
         */
        fun bind(item: Item) {
            val context = binding.root.context
            binding.itemName.text = item.name

            if (showStatus) {
                binding.itemStock.text = "${item.stock} left"
                binding.itemStock.visibility = View.VISIBLE
                binding.itemStatus.visibility = View.VISIBLE
                binding.itemId.visibility = View.GONE // Hide ID in full catalog view if preferred, or show it

                val statusStyle = when {
                    item.stock <= 0 -> StatusStyle(
                        chipColor = ContextCompat.getColor(context, R.color.item_reserved_chip),
                        textColor = ContextCompat.getColor(context, R.color.item_reserved_text),
                        textRes = R.string.status_out_of_stock
                    )
                    else -> StatusStyle(
                        chipColor = ContextCompat.getColor(context, R.color.item_available_chip),
                        textColor = ContextCompat.getColor(context, R.color.item_available_text),
                        textRes = R.string.status_available
                    )
                }

                binding.itemStatus.setText(statusStyle.textRes)
                binding.itemStatus.setTextColor(statusStyle.textColor)
                ViewCompat.setBackgroundTintList(
                    binding.itemStatus,
                    ColorStateList.valueOf(statusStyle.chipColor)
                )
            } else {
                binding.itemStock.visibility = View.GONE
                binding.itemStatus.visibility = View.GONE

                // Show ID only in "Borrowed items" view (when showStatus is false)
                binding.itemId.text = "ID: ${item.id}"
                binding.itemId.visibility = View.VISIBLE
            }

            if (item.returnDate != null && item.returnDate > 0) {
                val date = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(item.returnDate))
                binding.itemReturnDate.text = date // Just the date
                binding.itemReturnDate.visibility = View.VISIBLE
                binding.itemReturnDate.setTypeface(null, android.graphics.Typeface.BOLD)
                binding.itemReturnDate.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            } else {
                binding.itemReturnDate.visibility = View.GONE
            }

            (binding.root as? MaterialCardView)?.setOnLongClickListener {
                onLongPressDelete?.invoke(item)
                true
            }

            (binding.root as? MaterialCardView)?.setOnClickListener {
                showItemDialog(context, item)
            }
        }

        /**
         * Shows a dialog with item details and a link to instructions.
         */
        private fun showItemDialog(context: android.content.Context, item: Item) {
            MaterialAlertDialogBuilder(context)
                .setTitle(item.name)
                .setMessage("Would you like to view the instructions for this item?")
                .setPositiveButton("Show Instructions") { dialog, _ ->
                    val url = "https://www.raspberrypi.com/documentation/computers/getting-started.html"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowReservedItemBinding.inflate(inflater, parent, false)
        return ItemVH(binding)
    }

    override fun onBindViewHolder(holder: ItemVH, position: Int) = holder.bind(data[position])

    override fun getItemCount(): Int = data.size

    private data class StatusStyle(
        val chipColor: Int,
        val textColor: Int,
        val textRes: Int
    )
}
