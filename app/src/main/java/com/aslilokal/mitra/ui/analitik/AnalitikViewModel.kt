package com.aslilokal.mitra.ui.analitik

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.aslilokal.mitra.model.data.repository.KodelapoRepository
import com.aslilokal.mitra.model.remote.response.OrderResponse
import com.aslilokal.mitra.model.remote.response.RevenueResponse
import com.aslilokal.mitra.model.remote.response.VoucherItem
import com.aslilokal.mitra.model.remote.response.VoucherResponse
import com.aslilokal.mitra.utils.Resource
import com.aslilokal.mitra.utils.ResourcePagination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class AnalitikViewModel(private val mainRepository: KodelapoRepository) : ViewModel() {
    val latestOrderFinish: MutableLiveData<ResourcePagination<OrderResponse>> = MutableLiveData()
    var latestOrderResponse: OrderResponse? = null

    val revenueRequest: MutableLiveData<ResourcePagination<RevenueResponse>> = MutableLiveData()
    var allRevenueResponse: RevenueResponse? = null

    val vouchersRequest: MutableLiveData<ResourcePagination<VoucherResponse>> = MutableLiveData()
    var allVouchersResponse: VoucherResponse? = null

    suspend fun getAllLatestTransaction(
        token: String,
        idUser: String,
        month: Int,
        year: Int
    ) = viewModelScope.launch {
        latestOrderFinish.postValue(ResourcePagination.Loading())
        try {
            val response = mainRepository.getLatestTransaction(token, idUser, month, year)
            latestOrderFinish.postValue(handleDebtorResponse(response))
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> latestOrderFinish.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> latestOrderFinish.postValue(ResourcePagination.Error("Kesalahan tak terduga"))
            }
        }
    }

    private fun handleDebtorResponse(response: Response<OrderResponse>): ResourcePagination<OrderResponse> {
        if (response.body()?.success == true) {
            response.body()?.let { result ->
                if (latestOrderResponse == null) {
                    latestOrderResponse = result
                } else {
                    val oldDebtors = latestOrderResponse?.result
                    val newDebtors = result.result
                    if (newDebtors != oldDebtors) {
                        oldDebtors?.clear()
                        oldDebtors?.addAll(newDebtors)
                    }
                }
                return ResourcePagination.Success(latestOrderResponse ?: result)
            }
        }
        return ResourcePagination.Error(response.message())
    }

    fun getTotalRevenue(
        token: String,
        idUser: String
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getTotalRevenue(token, idUser)))
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> emit(Resource.error(data = null, message = "Jaringan lemah"))
                else -> emit(Resource.error(data = null, message = "Kesalahan tak terduga"))
            }
        }
    }

    suspend fun getAllRevenueRequest(
        token: String,
        idUser: String
    ) = viewModelScope.launch {
        revenueRequest.postValue(ResourcePagination.Loading())
        try {
            val response = mainRepository.getAllRequestRevenue(token, idUser)
            revenueRequest.postValue(handleRevenueResponse(response))
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> revenueRequest.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> revenueRequest.postValue(ResourcePagination.Error("Kesalahan tak terduga"))
            }
        }
    }

    private fun handleRevenueResponse(response: Response<RevenueResponse>): ResourcePagination<RevenueResponse> {
        if (response.body()?.success == true) {
            response.body()?.let { result ->
                if (allRevenueResponse == null) {
                    allRevenueResponse = result
                } else {
                    val oldRevenueList = allRevenueResponse?.result
                    val newRevenueList = result.result
                    if (newRevenueList != oldRevenueList) {
                        oldRevenueList?.clear()
                        oldRevenueList?.addAll(newRevenueList)
                    }
                }
                return ResourcePagination.Success(allRevenueResponse ?: result)
            }
        }
        return ResourcePagination.Error(response.message())
    }

    fun getAllVouchers(
        token: String,
        username: String
    ) = viewModelScope.launch {
        vouchersRequest.postValue(ResourcePagination.Loading())
        try {
            val response = mainRepository.getAllVouchers(token, username)
            vouchersRequest.postValue(handleVoucherResponse(response))
        } catch (exception: Exception) {
            Log.d("EXCEPTION", exception.message.toString())
            when (exception) {
                is IOException -> vouchersRequest.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> vouchersRequest.postValue(ResourcePagination.Error("Kesalahan tak terduga"))
            }
        }
    }

    private fun handleVoucherResponse(response: Response<VoucherResponse>): ResourcePagination<VoucherResponse>? {
        if (response.body()?.success == true) {
            response.body()?.let { result ->
                if (allVouchersResponse == null) {
                    allVouchersResponse = result
                } else {
                    val oldVoucherList = allVouchersResponse?.result
                    val newVoucherList = result.result
                    if (newVoucherList != oldVoucherList) {
                        oldVoucherList?.clear()
                        oldVoucherList?.addAll(newVoucherList)
                    }
                }
                return ResourcePagination.Success(allVouchersResponse ?: result)
            }
        }
        return ResourcePagination.Error(response.message())
    }

    fun postOneVoucher(
        token: String,
        oneVoucher: VoucherItem
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.postOneVoucher(token, oneVoucher)))
        } catch (exception: Exception) {
            Log.d("EXCEPTION", exception.message.toString())
            when (exception) {
                is IOException -> emit(Resource.error(data = null, message = "Jaringan lemah"))
                else -> emit(Resource.error(data = null, message = "Kesalahan tak terduga"))
            }
        }
    }

}