package ee.ut.cs.iotbazaar.ui.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ee.ut.cs.iotbazaar.databinding.RowReservedItemBinding
import ee.ut.cs.iotbazaar.model.Item

class ItemAdapter(
    private val onLongPressDelete: ((Item) -> Unit)? = null
) : RecyclerView.Adapter<ItemAdapter.ItemVH>() {

    private val data = mutableListOf<Item>()

    // Update the adapter's data and refresh the view
    fun submit(list: List<Item>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    // ViewHolder class for item rows
    inner class ItemVH(val binding: RowReservedItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.itemName.text = item.name
            // Set status label
            binding.itemStatus.text = if (item.reserved) "Reserved" else "Available"
            // Optional: different alpha or style could be added here
            // Set up long press listener for deletion
            (binding.root as? MaterialCardView)?.setOnLongClickListener {
                onLongPressDelete?.invoke(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowReservedItemBinding.inflate(inflater, parent, false)
        return ItemVH(binding)
    }

    override fun onBindViewHolder(holder: ItemVH, position: Int) = holder.bind(data[position])

    // Return the total number of items
    override fun getItemCount(): Int = data.size
}
