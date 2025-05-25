package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.usecase.user.IsCurrentUserAdminUseCase
import com.mgsanlet.cheftube.domain.usecase.user.LogoutUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.ui.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logout: LogoutUseCase,
    private val localeManager: LocaleManager,
    private val isCurrentUserAdmin: IsCurrentUserAdminUseCase
) : ViewModel() {

    private val _isAdmin = MutableLiveData<Boolean>(false)
    val isAdmin: LiveData<Boolean> = _isAdmin

    init {
        checkAdminStatus()
    }

    private fun checkAdminStatus() {
        viewModelScope.launch {
            val result = isCurrentUserAdmin()
            _isAdmin.value = result is DomainResult.Success && result.data
        }
    }

    fun setLocale(locale: Locale) {
        localeManager.setLocale(locale)
    }

    fun handleLogout() {
        logout()
    }
}