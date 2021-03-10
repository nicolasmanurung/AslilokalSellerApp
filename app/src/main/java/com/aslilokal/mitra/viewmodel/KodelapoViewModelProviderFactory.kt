package com.aslilokal.mitra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.repository.KodelapoRepository
import com.aslilokal.mitra.ui.account.AccountViewModel
import com.aslilokal.mitra.ui.account.login.LoginViewModel
import com.aslilokal.mitra.ui.analitik.AnalitikViewModel
import com.aslilokal.mitra.ui.debtor.DebtorViewModel
import com.aslilokal.mitra.ui.kelola.KelolaProdukViewModel
import com.aslilokal.mitra.ui.kelola.edit.EditViewModel
import com.aslilokal.mitra.ui.kelola.tambah.TambahProductViewModel
import com.aslilokal.mitra.ui.notifications.NotificationViewModel
import com.aslilokal.mitra.ui.pesanan.PesananViewModel
import com.aslilokal.mitra.ui.profil.ProfilViewModel

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
        if (modelClass.isAssignableFrom(PesananViewModel::class.java)) {
            return PesananViewModel(KodelapoRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(KodelapoRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(KodelapoRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(DebtorViewModel::class.java)) {
            return DebtorViewModel(KodelapoRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(AnalitikViewModel::class.java)) {
            return AnalitikViewModel(KodelapoRepository(apiHelper)) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}