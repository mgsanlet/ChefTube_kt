package com.mgsanlet.cheftube.ui.view.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.mgsanlet.cheftube.R
import androidx.core.view.isVisible
import com.mgsanlet.cheftube.databinding.ViewAccountSettingsBinding

class AccountSettingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAccountSettingsBinding = ViewAccountSettingsBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        setUpListeners()
    }

    private fun setUpListeners(){
        binding.expandToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.accountBody.visibility = VISIBLE
                binding.expandToggleButton.setBackgroundResource(R.drawable.ic_arrow_v3_up_24)
            } else {
                binding.accountBody.visibility = GONE
                binding.expandToggleButton.setBackgroundResource(R.drawable.ic_arrow_v3_down_24)
            }
        }
    }

    fun setOnPasswordClickListener(listener: () -> Unit) {
        binding.changePasswordButton.setOnClickListener {
            listener()
        }
    }

    fun setOnDeleteClickListener(listener: () -> Unit) {
        binding.deleteAccountButton.setOnClickListener {
            listener()
        }
    }

    fun setOnExpandDownClickListener(listener: () -> Unit) {
        binding.expandToggleButton.setOnClickListener {
            if (binding.accountBody.isVisible) {
                listener()
            }
        }
    }
}