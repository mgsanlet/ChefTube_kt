package com.mgsanlet.cheftube.ui.view.base

import android.text.Editable
import android.util.Patterns
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.User

abstract class BaseFormFragment<T: ViewBinding, VM : ViewModel> : BaseFragment<T, VM>() {

    abstract fun isValidViewInput(): Boolean

    fun areFieldsEmpty(fields: List<Editable>):Boolean{
        var hasEmptyFields = false
        for (field in fields){
            if (field is EditText){
                if (field.text.trim().isEmpty()){
                    field.error = getString(R.string.required)
                    hasEmptyFields = true
                }
            }
        }
        return hasEmptyFields
    }

    fun isValidEmailPattern(email: EditText): Boolean{
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
            email.error = getString(R.string.invalid_email)
            return false
        }
        return true
    }

    fun isValidPasswordPattern(password: EditText): Boolean{
        // Verificar longitud mínima
        if (password.text.trim().length < User.PASSWORD_MIN_LENGTH) {
            password.error = getString(R.string.short_pwd)
            return false
        }
        return true
    }

}