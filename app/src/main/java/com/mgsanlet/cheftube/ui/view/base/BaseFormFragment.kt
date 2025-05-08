package com.mgsanlet.cheftube.ui.view.base

import android.widget.EditText
import androidx.viewbinding.ViewBinding
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.ui.util.isEmpty

abstract class BaseFormFragment<T : ViewBinding> : BaseFragment<T>() {

    protected abstract fun isValidViewInput(): Boolean

    protected fun areFieldsEmpty(fields: List<EditText>): Boolean {
        var hasEmptyFields = false
        for (field in fields) {

            if (field.text.isBlank()) {
                field.error = getString(R.string.required)
                hasEmptyFields = true
            }

        }
        return hasEmptyFields
    }
}