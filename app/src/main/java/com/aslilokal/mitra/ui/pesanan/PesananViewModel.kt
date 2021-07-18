package com.aslilokal.mitra.ui.pesanan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.mitra.model.data.repository.AslilokalRepository
import com.aslilokal.mitra.model.remote.request.PesananRequest
import com.aslilokal.mitra.model.remote.response.OrderResponse
import com.aslilokal.mitra.model.remote.response.StatusResponse
import com.aslilokal.mitra.utils.ResourcePagination
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class PesananViewModel(private var mainRepository: AslilokalRepository) : ViewModel() {
    val orders: MutableLiveData<ResourcePagination<OrderResponse>> = MutableLiveData()
    var orderResponse: OrderResponse? = null

    var orderStatusResponse: MutableLiveData<ResourcePagination<StatusResponse>> = MutableLiveData()
    var resStatus: StatusResponse? = null

    suspend fun getPesanan(
        token: String,
        idUser: String,
        status: String
    ) = viewModelScope.launch {
        orders.postValue(ResourcePagination.Loading())
        try {
            val response = mainRepository.getOrder(token, idUser, status)
            orders.postValue(handleOrderResponse(response))
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> orders.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> orders.postValue(ResourcePagination.Error("Kesalahan tak terduga"))
            }
        }
    }

    private fun handleOrderResponse(response: Response<OrderResponse>): ResourcePagination<OrderResponse> {
        if (response.body()?.success == true) {
            response.body()?.let { resultOrder ->
                if (orderResponse == null) {
                    orderResponse = resultOrder
                } else {
                    val oldOrders = orderResponse?.result
                    val newOrders = resultOrder.result
                    if (!(newOrders.isNullOrEmpty()) && newOrders != oldOrders) {
                        oldOrders?.clear()
                        oldOrders?.addAll(newOrders)
                    }
                }
                return ResourcePagination.Success(orderResponse ?: resultOrder)
            }
        }
        return ResourcePagination.Error(response.message())
    }

    suspend fun editStatusPesanan(
        token: String,
        idOrder: String,
        pesananRequest: PesananRequest
    ) = viewModelScope.launch {
        orderStatusResponse.postValue(ResourcePagination.Loading())
        try {
            val response = mainRepository.putStatusOrder(token, idOrder, pesananRequest)
            orderStatusResponse.postValue(handleStatusResponse(response))
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> orderStatusResponse.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> orderStatusResponse.postValue(ResourcePagination.Error("Kesalahan tak terduga"))
            }
        }
    }

    private fun handleStatusResponse(response: Response<StatusResponse>): ResourcePagination<StatusResponse> {
        if (response.body()?.success == true) {
            if (resStatus == null) {
                response.body()?.let { statusResponse ->
                    resStatus = statusResponse

                    return ResourcePagination.Success(resStatus ?: statusResponse)
                }
            }
        }
        return ResourcePagination.Error(response.message())
    }
}