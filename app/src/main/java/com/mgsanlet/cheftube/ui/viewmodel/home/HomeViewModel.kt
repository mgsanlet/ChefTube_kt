package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import com.mgsanlet.cheftube.domain.usecase.user.LogoutUseCase
import com.mgsanlet.cheftube.ui.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logout: LogoutUseCase,
    private val localeManager: LocaleManager
) : ViewModel() {

    fun setLocale(locale: Locale) {
        localeManager.setLocale(locale)
    }

    fun handleLogout() {
        logout()
    }
}