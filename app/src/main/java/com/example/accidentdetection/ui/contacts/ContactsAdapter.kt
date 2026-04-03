package com.example.accidentdetection.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.accidentdetection.data.local.EmergencyContact
import com.example.accidentdetection.databinding.ItemContactBinding

class ContactsAdapter(
    private val onEdit: (EmergencyContact) -> Unit,
    private val onDelete: (EmergencyContact) -> Unit,
) : ListAdapter<EmergencyContact, ContactsAdapter.ContactViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        private val binding: ItemContactBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: EmergencyContact) {
            binding.textName.text = contact.name
            binding.textPhone.text = contact.phoneNumber
            binding.textRelationship.text = contact.relationship
            binding.buttonEdit.setOnClickListener { onEdit(contact) }
            binding.buttonDelete.setOnClickListener { onDelete(contact) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem == newItem
        }
    }
}
