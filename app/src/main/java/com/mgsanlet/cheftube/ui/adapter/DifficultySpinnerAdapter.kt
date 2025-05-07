package com.mgsanlet.cheftube.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mgsanlet.cheftube.databinding.DifficultySpinnerDropdownItemBinding
import com.mgsanlet.cheftube.databinding.DifficultySpinnerHeaderBinding

class DifficultySpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: DifficultySpinnerHeaderBinding
        val view: View

        if (convertView == null) {
            binding = DifficultySpinnerHeaderBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as DifficultySpinnerHeaderBinding
        }
        binding.spinnerHeader.text = items[position]

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: DifficultySpinnerDropdownItemBinding
        val view: View

        if (convertView == null) {
            binding = DifficultySpinnerDropdownItemBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as DifficultySpinnerDropdownItemBinding
        }

        binding.spinnerDropdownItem.text = items[position]
        return view
    }
}
