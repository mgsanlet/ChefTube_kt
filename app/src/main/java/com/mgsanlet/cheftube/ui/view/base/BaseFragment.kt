package com.mgsanlet.cheftube.ui.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T: ViewBinding, VM : ViewModel> : Fragment() {

    private var _binding: T? = null
    protected val binding get() = _binding!!
    protected lateinit var viewModel: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateViewBinding(inflater, container)
        viewModel = defineViewModel()
        setUpObservers()
        setUpListeners()
        setUpViewProperties()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    abstract fun defineViewModel(): VM

    protected open fun setUpObservers() {}

    protected open fun setUpListeners() {}

    protected open fun setUpViewProperties() {}


}