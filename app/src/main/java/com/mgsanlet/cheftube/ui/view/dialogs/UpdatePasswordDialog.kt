package com.mgsanlet.cheftube.ui.view.dialogs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.hideKeyboard
import com.mgsanlet.cheftube.ui.view.home.EditProfileFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel

typealias OnPasswordUpdatedListener = () -> Unit

class UpdatePasswordDialog(val fragment: EditProfileFragment) : BaseAccountDialog() {

    private val viewModel: ProfileViewModel by activityViewModels()
    private var onPasswordUpdatedListener: OnPasswordUpdatedListener? = null

    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    fun setOnPasswordUpdatedListener(listener: OnPasswordUpdatedListener) {
        onPasswordUpdatedListener = listener
    }

    @SuppressLint("InflateParams")
    override fun createDialogView(): View {
        val inflater = LayoutInflater.from(fragment.requireContext())
        val view = inflater.inflate(R.layout.dialog_change_password, null)

        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        return view
    }

    override fun setupListeners() {

        saveButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()


            var hasError = false

            // Validar contraseña actual
            if (currentPassword.isEmpty()) {
                currentPasswordEditText.error = getString(R.string.required)
                hasError = true
            } else {
                currentPasswordEditText.error = null
            }

            // Validar nueva contraseña
            if (newPassword.isBlank()) {
                newPasswordEditText.error = getString(R.string.required)
                hasError = true
            } else {
                newPasswordEditText.error = null
            }

            // Validar confirmación de contraseña
            if (confirmPassword.isBlank()) {
                confirmPasswordEditText.error = getString(R.string.required)
                hasError = true
            } else if (newPassword != confirmPassword) {
                confirmPasswordEditText.error = getString(R.string.pwd_d_match)
                hasError = true
            } else {
                confirmPasswordEditText.error = null
            }

            if (!hasError) {
                updatePassword(currentPassword, newPassword)
            }
        }

        cancelButton.setOnClickListener { dismiss() }
    }

    private fun updatePassword(currentPassword: String, newPassword: String) {

        viewModel.updateUserPassword(
            currentPassword,
            newPassword
        )

        // Observar los cambios en el ViewModel
        viewModel.uiState.observe(fragment.viewLifecycleOwner) { state ->
            when (state) {

                is ProfileState.PasswordUpdated -> {
                    view?.hideKeyboard()
                    onPasswordUpdatedListener?.invoke()
                    dismiss()
                }

                is ProfileState.Error -> {
                    when (state.error) {
                        is UserError.WrongCredentials -> {
                            currentPasswordEditText.error = state.error.asMessage(fragment.requireContext())
                        }

                        is UserError.PasswordTooShort -> {
                            newPasswordEditText.error = state.error.asMessage(fragment.requireContext())
                        }

                        is UserError.InvalidPasswordPattern -> {
                            newPasswordEditText.error = state.error.asMessage(fragment.requireContext())
                        }

                        else -> {
                            currentPasswordEditText.error = state.error.asMessage(fragment.requireContext())
                        }
                    }
                }

                else -> { }
            }
        }
    }

    override fun showLoading(show: Boolean) {
        super.showLoading(show)
        saveButton.isEnabled = !show
        cancelButton.isEnabled = !show
    }

    companion object {
        const val TAG = "UpdatePasswordDialog"
    }
}
