package ee.ut.cs.iotbazaar.ui.inbox

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ee.ut.cs.iotbazaar.data.entities.User
import ee.ut.cs.iotbazaar.databinding.ItemUserBinding
import ee.ut.cs.iotbazaar.ui.inbox.InboxAdapter.*

/**
 * RecyclerView Adapter for displaying a list of users in the Inbox.
 * Uses ListAdapter for efficient updates.
 */
class InboxAdapter :
    ListAdapter<User, InboxViewHolder>(UserDiffCallback) {

    /**
     * Creates a new ViewHolder for a user item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InboxViewHolder(binding)
    }

    /**
     * Binds the user data to the ViewHolder at the specified position.
     */
    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder class for caching view references.
     */
    class InboxViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a User object to the view.
         */
        fun bind(user: User) {
            binding.userName.text = user.name
            binding.userAge.text = user.age.toString()
        }
    }

    /**
     * DiffUtil callback for calculating changes between lists of Users.
     */
    private object UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem == newItem
    }
}