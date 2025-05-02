package com.mgsanlet.cheftube.ui.view.home

import com.mgsanlet.cheftube.databinding.FragmentManageAccountBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ManageAccountFragment @Inject constructor() : BaseFragment<FragmentManageAccountBinding>() {

    private lateinit var sharedViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentManageAccountBinding = FragmentManageAccountBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        sharedViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Error -> {
                    val errorMessage = state.error.asMessage(requireContext())
                    showLoading(false)
                    when (state.error) {


                        else -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                is ProfileState.Loading -> showLoading(true)

                is ProfileState.LoadSuccess -> {
                    showLoading(false)
                }

                is ProfileState.SaveSuccess -> {
                    FragmentNavigator.loadFragment(
                        null, this,
                        ProfileFragment(), R.id.fragmentContainerView
                    )
                }
            }
        }
    }

    override fun setUpListeners() {

    }

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}