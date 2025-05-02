package com.mgsanlet.cheftube.ui.view.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentEditProfileBinding
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ProfileFragment permite al usuario ver y modificar los detalles de su perfil,
 * incluyendo nombre de usuario, email y contraseña.
 */
@AndroidEntryPoint
class EditProfileFragment @Inject constructor() : BaseFragment<FragmentEditProfileBinding>() {

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
    ): FragmentEditProfileBinding = FragmentEditProfileBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        sharedViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Error -> {
                    val errorMessage = state.error.asMessage(requireContext())
                    showLoading(false)
                    when (state.error) {

                        is UserError.UsernameInUse -> binding.usernameEditText.error = errorMessage
                        is UserError.InvalidUsernamePattern -> binding.usernameEditText.error =
                            errorMessage

                        else -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                is ProfileState.Loading -> showLoading(true)
                is ProfileState.LoadSuccess -> {
                    showLoading(false)
                    showUserData()
                }

                is ProfileState.SaveSuccess -> {
                    Toast.makeText(activity, getString(R.string.data_saved), Toast.LENGTH_SHORT)
                        .show()
                    FragmentNavigator.loadFragment(
                        null, this,
                        ProfileFragment(), R.id.fragmentContainerView
                    )
                }
            }
        }
    }

    override fun setUpListeners() {
        binding.saveButton.setOnClickListener {
            sharedViewModel.tryUpdateUserData(
                binding.usernameEditText.text.toString(),
                binding.bioEditText.text.toString()
            )
        }
        binding.cancelButton.setOnClickListener {
            FragmentNavigator.loadFragment(
                null, this,
                ProfileFragment(), R.id.fragmentContainerView
            )
        }
    }

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
    }


    private fun showUserData() {
        sharedViewModel.userData.value?.let {
            //binding.profilePictureImageView.loadUrl(user.profilePictureUrl, requireContext())
            binding.usernameEditText.setText(it.username)
            binding.bioEditText.setText(it.bio)
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}