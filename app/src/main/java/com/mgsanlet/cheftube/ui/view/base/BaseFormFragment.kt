package com.mgsanlet.cheftube.ui.view.base

import android.util.Patterns
import android.widget.EditText
import androidx.viewbinding.ViewBinding

import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.UserDto
import com.mgsanlet.cheftube.ui.util.isEmpty

abstract class BaseFormFragment<T : ViewBinding> : BaseFragment<T>() {

    protected abstract fun isValidViewInput(): Boolean

    protected fun areFieldsEmpty(fields: List<EditText>): Boolean {
        var hasEmptyFields = false
        for (field in fields) {

            if (field.isEmpty()) {
                field.error = getString(R.string.required)
                hasEmptyFields = true
            }

        }
        return hasEmptyFields
    }

    protected fun isValidEmailPattern(email: EditText): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
            email.error = getString(R.string.invalid_email)
            return false
        }
        return true
    }

    protected fun isValidPasswordPattern(password: EditText): Boolean {
        // Verificar longitud mínima
        if (password.text.trim().length < UserDto.PASSWORD_MIN_LENGTH) {
            password.error = getString(R.string.short_pwd)
            return false
        }
        return true
    }

}