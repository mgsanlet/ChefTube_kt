package com.mgsanlet.cheftube.ui.view.dialogs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.hideKeyboard
import com.mgsanlet.cheftube.ui.view.home.EditProfileFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel

typealias OnAccountDeletedListener = () -> Unit

class DeleteAccountDialog(val fragment: EditProfileFragment) : BaseAccountDialog() {

    private val viewModel: ProfileViewModel by activityViewModels()
    private var onAccountDeletedListener: OnAccountDeletedListener? = null

    private lateinit var passwordEditText: EditText
    private lateinit var confirmationCheckbox: TextView
    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button
    private var isConfirmed = false

    fun setOnAccountDeletedListener(listener: OnAccountDeletedListener) {
        onAccountDeletedListener = listener
    }

    @SuppressLint("InflateParams")
    override fun createDialogView(): View {
        val inflater = LayoutInflater.from(fragment.requireContext())
        val view = inflater.inflate(R.layout.dialog_delete_account, null, false)

        passwordEditText = view.findViewById(R.id.currentPasswordEditText)
        confirmationCheckbox = view.findViewById(R.id.confirmationCheckbox)
        deleteButton = view.findViewById(R.id.deleteButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        // Configurar el checkbox de confirmación
        confirmationCheckbox.setOnClickListener {
            isConfirmed = !isConfirmed
            updateConfirmationCheckbox()
        }

        return view
    }

    private fun updateConfirmationCheckbox() {
        val drawableRes = if (isConfirmed) {
            R.drawable.ic_checkbox_checked_24
        } else {
            R.drawable.ic_checkbox_unchecked_24
        }

        val drawable = ResourcesCompat.getDrawable(resources, drawableRes, null)
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        confirmationCheckbox.setCompoundDrawables(drawable, null, null, null)
    }

    override fun setupListeners() {

        deleteButton.setOnClickListener {
            val password = passwordEditText.text.toString().trim()

            if (password.isBlank()) {
                passwordEditText.error = getString(R.string.required)
                return@setOnClickListener
            }

            if (!isConfirmed) {
                Toast.makeText(fragment.requireContext(), R.string.please_confirm_deletion, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showConfirmDeleteDialog(password)
        }

        cancelButton.setOnClickListener { dismiss() }
    }

    private fun showConfirmDeleteDialog(password: String) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.delete_account)
            .setMessage(R.string.delete_account_confirmation_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteAccount(password)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteAccount(password: String) {
        enableButtons(false)
        viewModel.deleteUserAccount(password)

        // Observar los cambios en el ViewModel
        viewModel.uiState.observe(fragment.viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.AccountDeleted -> {
                    enableButtons(true)
                    view?.hideKeyboard()
                    onAccountDeletedListener?.invoke()
                    dismiss()
                }

                is ProfileState.Error -> {
                    enableButtons(true)
                    passwordEditText.error = state.error.asMessage(fragment.requireContext())
                }

                else -> { /* No hacer nada para otros estados */
                }
            }
        }
    }

    private fun enableButtons(enable: Boolean) {
        (dialog as? android.app.AlertDialog)?.apply {
            getButton(android.app.AlertDialog.BUTTON_POSITIVE).isEnabled = enable
            getButton(android.app.AlertDialog.BUTTON_NEGATIVE).isEnabled = enable
        }
    }

    companion object {
        const val TAG = "DeleteAccountDialog"
    }
}
