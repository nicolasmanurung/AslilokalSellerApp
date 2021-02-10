package com.kodelapo.mitra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kodelapo.mitra.model.data.api.ApiHelper
import com.kodelapo.mitra.model.data.repository.KodelapoRepository
import com.kodelapo.mitra.ui.account.login.LoginViewModel
import com.kodelapo.mitra.ui.kelola.KelolaProdukViewModel
import com.kodelapo.mitra.ui.kelola.edit.EditViewModel
import com.kodelapo.mitra.ui.kelola.tambah.TambahProductViewModel
import com.kodelapo.mitra.ui.profil.ProfilViewModel

class KodelapoViewModelProviderFactory(private val apiHelper: ApiHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(KodelapoRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(KelolaProdukViewModel::class.java)) {
            return KelolaProdukViewModel(KodelapoRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(ProfilViewModel::class.java)) {
            return ProfilViewModel(KodelapoRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(TambahProductViewModel::class.java)) {
            return TambahProductViewModel(KodelapoRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
            return EditViewModel(KodelapoRepository(apiHelper)) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}