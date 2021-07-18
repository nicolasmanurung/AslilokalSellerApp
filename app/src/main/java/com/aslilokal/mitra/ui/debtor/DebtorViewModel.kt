package com.aslilokal.mitra.ui.debtor

import androidx.lifecycle.*
import com.aslilokal.mitra.model.data.repository.AslilokalRepository
import com.aslilokal.mitra.model.remote.response.DebtorItem
import com.aslilokal.mitra.model.remote.response.DebtorResponse
import com.aslilokal.mitra.utils.Resource
import com.aslilokal.mitra.utils.ResourcePagination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class DebtorViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val debtors: MutableLiveData<ResourcePagination<DebtorResponse>> = MutableLiveData()
    var debtorResponse: DebtorResponse? = null


    suspend fun getDebtor(
        token: String,
        idUser: String,
        year: String,
        month: String
    ) = viewModelScope.launch {
        debtors.postValue(ResourcePagination.Loading())
        try {
            val response = mainRepository.getDebtorByMonth(token, idUser, year, month)
            debtors.postValue(handleDebtorResponse(response))
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> debtors.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> debtors.postValue(ResourcePagination.Error("Kesalahan tak terduga"))
            }
        }
    }

    private fun handleDebtorResponse(response: Response<DebtorResponse>): ResourcePagination<DebtorResponse> {
        if (response.body()?.success == true) {
            response.body()?.let { result ->
                if (debtorResponse == null) {
                    debtorResponse = result
                } else {
                    val oldDebtors = debtorResponse?.result
                    val newDebtors = result.result
                    if (!(newDebtors.isNullOrEmpty()) && newDebtors != oldDebtors) {
                        oldDebtors?.clear()
                        oldDebtors?.addAll(newDebtors)
                    }
                }
                return ResourcePagination.Success(debtorResponse ?: result)
            }
        }
        return ResourcePagination.Error(response.message())
    }

    fun postOneDebtor(token: String, debtor: DebtorItem) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.postOneDebtor(token, debtor)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }

    fun putOneDebtor(
        token: String,
        idDebtor: String,
        debtor: DebtorItem
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.putOneDebtor(token, idDebtor, debtor)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }

}