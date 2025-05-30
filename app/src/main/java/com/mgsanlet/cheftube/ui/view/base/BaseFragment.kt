package com.mgsanlet.cheftube.ui.view.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    private var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = inflateViewBinding(inflater, container)
        setUpObservers()
        setUpListeners()
        setUpViewProperties()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    protected open fun setUpObservers() {}

    protected open fun setUpListeners() {}

    protected open fun setUpViewProperties() {}

    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun showToast(@StringRes messageRes: Int) {
        showToast(getString(messageRes))
    }
}