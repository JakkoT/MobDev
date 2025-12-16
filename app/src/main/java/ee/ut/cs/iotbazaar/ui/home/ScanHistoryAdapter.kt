package ee.ut.cs.iotbazaar.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ee.ut.cs.iotbazaar.databinding.RowScanHistoryItemBinding
import ee.ut.cs.iotbazaar.model.ScanHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView Adapter for displaying the user's scan history.
 * Uses ListAdapter for efficient updates using DiffUtil.
 */
class ScanHistoryAdapter : ListAdapter<ScanHistoryItem, ScanHistoryAdapter.ViewHolder>(DiffCallback()) {

    /**
     * Creates a new ViewHolder for a scan history item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowScanHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Binds the data to the ViewHolder at the specified position.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder class for caching view references.
     */
    class ViewHolder(private val binding: RowScanHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())

        /**
         * Binds a ScanHistoryItem to the view.
         * Sets text and color based on the action type (RETURN vs BORROW).
         */
        fun bind(item: ScanHistoryItem) {
            binding.itemName.text = item.itemName
            binding.itemId.text = "ID: ${item.itemId}"
            binding.timestamp.text = dateFormat.format(Date(item.timestamp))

            if (item.action == "RETURN") {
                binding.actionType.text = "Returned"
                binding.actionType.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
            } else {
                binding.actionType.text = "Borrowed"
                binding.actionType.setTextColor(binding.root.context.getColor(android.R.color.holo_blue_dark))
            }
        }
    }

    /**
     * DiffUtil callback for calculating changes between lists of ScanHistoryItems.
     */
    class DiffCallback : DiffUtil.ItemCallback<ScanHistoryItem>() {
        override fun areItemsTheSame(oldItem: ScanHistoryItem, newItem: ScanHistoryItem): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.itemId == newItem.itemId
        }

        override fun areContentsTheSame(oldItem: ScanHistoryItem, newItem: ScanHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
