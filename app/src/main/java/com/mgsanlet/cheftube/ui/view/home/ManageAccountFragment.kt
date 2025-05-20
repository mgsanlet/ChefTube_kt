package com.mgsanlet.cheftube.ui.view.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentManageAccountBinding
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.view.dialogs.DeleteAccountDialog
import com.mgsanlet.cheftube.ui.view.dialogs.UpdateEmailDialog
import com.mgsanlet.cheftube.ui.view.dialogs.UpdatePasswordDialog
import com.mgsanlet.cheftube.ui.view.auth.AuthActivity
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageAccountFragment @Inject constructor() : BaseFragment<FragmentManageAccountBinding>() {

    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentManageAccountBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Loading -> {
                    // Mostrar u ocultar loading si es necesario
                }
                is ProfileState.Error -> {
                    showToast(state.error.asMessage(requireContext()))
                }
                is ProfileState.EmailUpdated -> {
                    showToast(R.string.email_updated_successfully)
                }
                is ProfileState.PasswordUpdated -> {
                    showToast(R.string.password_updated_successfully)
                }
                is ProfileState.AccountDeleted -> {
                    // Navegar a la pantalla de inicio de sesión
                    startActivity(Intent(requireContext(), AuthActivity::class.java))
                    requireActivity().finish()
                }
                else -> { /* No hacer nada para otros estados */ }
            }
        }
    }



    override fun setUpListeners() {
        binding.changeEmailButton.setOnClickListener {
            showUpdateEmailDialog()
        }

        binding.changePasswordButton.setOnClickListener {
            showUpdatePasswordDialog()
        }

        binding.deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showUpdateEmailDialog() {
        val dialog = UpdateEmailDialog.newInstance()
        dialog.setOnEmailUpdatedListener {
            // Actualizar la interfaz de usuario si es necesario
            viewModel.loadCurrentUserData()
        }
        dialog.show(parentFragmentManager, UpdateEmailDialog.TAG)
    }

    private fun showUpdatePasswordDialog() {
        val dialog = UpdatePasswordDialog.newInstance()
        dialog.setOnPasswordUpdatedListener {
            // Mostrar mensaje de éxito
            showToast(R.string.password_updated_successfully)
        }
        dialog.show(parentFragmentManager, UpdatePasswordDialog.TAG)
    }

    private fun showDeleteAccountDialog() {
        val dialog = DeleteAccountDialog.newInstance()
        dialog.setOnAccountDeletedListener {
            // La navegación se maneja en el observer del estado AccountDeleted
        }
        dialog.show(parentFragmentManager, DeleteAccountDialog.TAG)
    }
}