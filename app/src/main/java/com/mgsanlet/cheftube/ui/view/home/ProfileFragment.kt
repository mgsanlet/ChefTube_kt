package com.mgsanlet.cheftube.ui.view.home

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentProfileBinding
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ProfileFragment permite al usuario ver y modificar los detalles de su perfil,
 * incluyendo nombre de usuario, email y contraseña.
 */
@AndroidEntryPoint
class ProfileFragment @Inject constructor() : BaseFragment<FragmentProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModels()
    private var editDialog: AlertDialog? = null
    private var usernameEditText: EditText? = null

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Error -> {
                    val errorMessage = state.error.asMessage(requireContext())
                    showLoading(false)
                    when (state.error) {

                        is UserError.UsernameInUse -> usernameEditText?.error = errorMessage
                        is UserError.InvalidUsernamePattern -> usernameEditText?.error = errorMessage
                        else -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                is ProfileState.Loading -> showLoading(true)
                is ProfileState.LoadSuccess -> {
                    showLoading(false)
                    showUserData()
                }
                is ProfileState.SaveSuccess -> {
                    showLoading(false)
                    showUserData()
                    Toast.makeText(context, getString(R.string.data_saved), Toast.LENGTH_SHORT)
                        .show()
                    editDialog?.dismiss()
                }
            }
        }
    }

    override fun setUpListeners() {
        binding.editButton.setOnClickListener { showEditFormDialog() }
    }

    private fun showEditFormDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())

        // Inflar un diseño personalizado para el diálogo de búsqueda
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null)
        dialogBuilder.setView(dialogView)

        // Obtener una referencia a los elementos de la interfaz de usuario en el diseño personalizado
        usernameEditText = dialogView.findViewById<EditText>(R.id.usernameEditText)
        usernameEditText?.setText(binding.usernameTextView.text.toString())
        val bioEditText = dialogView.findViewById<EditText>(R.id.bioEditText)
        bioEditText.setText(binding.bioTextView.text.toString())

        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        editDialog = dialogBuilder.create()
        editDialog?.show()

        saveButton.setOnClickListener {
            viewModel.tryUpdateUserData(usernameEditText?.text.toString(), bioEditText.text.toString())
        }
        cancelButton.setOnClickListener {
            editDialog?.dismiss()
        }
    }

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
    }


    private fun showUserData() {
        viewModel.userData.value?.let{
            //binding.profilePictureImageView.loadUrl(user.profilePictureUrl, requireContext())
            binding.usernameTextView.text = it.username
            binding.emailTextView.text = it.email
            binding.followersTextView.text = getString(R.string.followers, 43) //TODO
            binding.followingTextView.text = getString(R.string.following, 2) //TODO
            binding.bioTextView.text = it.bio
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