package com.mgsanlet.cheftube.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.ItemInactiveUserBinding
import com.mgsanlet.cheftube.domain.model.DomainUser
import java.text.SimpleDateFormat
import java.util.*

class InactiveUsersAdapter(
    private val onDeleteClick: (DomainUser) -> Unit
) : ListAdapter<DomainUser, InactiveUsersAdapter.InactiveUserViewHolder>(InactiveUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InactiveUserViewHolder {
        val binding = ItemInactiveUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InactiveUserViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: InactiveUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InactiveUserViewHolder(
        private val binding: ItemInactiveUserBinding,
        private val onDeleteClick: (DomainUser) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.getDefault())
        
        fun bind(user: DomainUser) {
            with(binding) {
                // Mostrar información del usuario
                textUsername.text = user.username
                textEmail.text = user.email
                
                // Mostrar días inactivos
                textInactiveDays.text = root.context.getString(
                    R.string.days_inactive_format,
                    user.inactiveDays
                )
                
                // Mostrar días inactivos en el campo de último inicio de sesión
                textLastLogin.text = root.context.getString(
                    R.string.days_inactive_format,
                    user.inactiveDays
                )
                
                // Set delete button click listener
                buttonDelete.setOnClickListener { onDeleteClick(user) }
                
                // Set click listeners for the entire item if needed
                root.setOnClickListener {
                    // Handle item click if needed
                }
            }
        }
    }
}

private class InactiveUserDiffCallback : DiffUtil.ItemCallback<DomainUser>() {
    override fun areItemsTheSame(oldItem: DomainUser, newItem: DomainUser): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DomainUser, newItem: DomainUser): Boolean {
        return oldItem == newItem
    }
}
