package com.mgsanlet.cheftube.ui.view.base

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.mgsanlet.cheftube.R

abstract class BaseFragment<T : ViewBinding, VM : ViewModel> : Fragment() {

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

    protected fun setUpProgressBar(progressBar: ProgressBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            progressBar.indeterminateDrawable.colorFilter =
                BlendModeColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.primary_green),
                    BlendMode.SRC_IN
                )
        } else {
            @Suppress("DEPRECATION") // Solo para versiones antiguas
            progressBar.indeterminateDrawable.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primary_green)
                , android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }


}