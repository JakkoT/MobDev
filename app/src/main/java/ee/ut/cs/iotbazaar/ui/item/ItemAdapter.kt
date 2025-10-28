package ee.ut.cs.iotbazaar.ui.item

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.databinding.RowReservedItemBinding
import ee.ut.cs.iotbazaar.model.Item

class ItemAdapter(
    private val onLongPressDelete: ((Item) -> Unit)? = null
) : RecyclerView.Adapter<ItemAdapter.ItemVH>() {

    private val data = mutableListOf<Item>()

    fun submit(list: List<Item>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class ItemVH(private val binding: RowReservedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            val context = binding.root.context
            binding.itemName.text = item.name

            val statusStyle = if (item.reserved) {
                StatusStyle(
                    chipColor = ContextCompat.getColor(context, R.color.item_reserved_chip),
                    textColor = ContextCompat.getColor(context, R.color.item_reserved_text),
                    textRes = R.string.status_reserved
                )
            } else {
                StatusStyle(
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

    override fun getItemCount(): Int = data.size

    private data class StatusStyle(
        val chipColor: Int,
        val textColor: Int,
        val textRes: Int
    )
}
