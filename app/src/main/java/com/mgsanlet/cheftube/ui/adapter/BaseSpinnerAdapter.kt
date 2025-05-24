package com.mgsanlet.cheftube.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mgsanlet.cheftube.databinding.SpinnerHeaderBinding
import com.mgsanlet.cheftube.databinding.SpinnerItemBinding

class BaseSpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: SpinnerHeaderBinding
        val view: View

        if (convertView == null) {
            binding = SpinnerHeaderBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as SpinnerHeaderBinding
        }
        binding.spinnerHeader.text = items[position]

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: SpinnerItemBinding
        val view: View

        if (convertView == null) {
            binding = SpinnerItemBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as SpinnerItemBinding
        }

        binding.spinnerDropdownItem.text = items[position]
        return view
    }
}
