package com.example.safetymanagement2022.ui.list.glass

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safetymanagement2022.data.remote.model.response.SmartGlass
import com.example.safetymanagement2022.databinding.ItemListGlassBinding

class ListSmartGlassAdapter(val admin: Int): ListAdapter<SmartGlass, ListSmartGlassAdapter.ListSmartGlassViewHolder>(
    ListBuildingDiffCallback()) {
    private lateinit var binding: ItemListGlassBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSmartGlassViewHolder {
        binding = ItemListGlassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListSmartGlassViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListSmartGlassViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ListSmartGlassViewHolder(private val binding: ItemListGlassBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(smartGlass: SmartGlass) {
            binding.smartglass = smartGlass
            binding.admin = admin
            binding.executePendingBindings()
        }
    }
}

class ListBuildingDiffCallback: DiffUtil.ItemCallback<SmartGlass>() {
    override fun areItemsTheSame(oldItem: SmartGlass, newItem: SmartGlass): Boolean {
        return oldItem.glassId == newItem.glassId
    }
    override fun areContentsTheSame(oldItem: SmartGlass, newItem: SmartGlass): Boolean {
        return oldItem == newItem
    }
}