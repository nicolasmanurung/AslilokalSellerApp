package com.aslilokal.mitra.ui.kelola

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.mitra.model.data.repository.AslilokalRepository
import com.aslilokal.mitra.model.remote.response.ProductResponse
import com.aslilokal.mitra.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.aslilokal.mitra.utils.ResourcePagination
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class KelolaProdukViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val products: MutableLiveData<ResourcePagination<ProductResponse>> = MutableLiveData()
    var productPage = 1
    var productResponse: ProductResponse? = null

    fun getProducts(token: String, idSellerAccount: String, type: String) = viewModelScope.launch {
        breakingProductCall(token, idSellerAccount, type)
    }

    private suspend fun breakingProductCall(token: String, idSellerAccount: String, type: String) {
        products.postValue(ResourcePagination.Loading())
        try {
            // Next Update Internet Connection
//            if(hasInternetConnection()) {
            val response = mainRepository.getProduct(token, idSellerAccount, type, productPage, QUERY_PAGE_SIZE)
            products.postValue(handleProductResponse(response))
//            } else {
//                breakingNews.postValue(Resource.Error("No internet connection"))
//            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> products.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> products.postValue(ResourcePagination.Error("Conversion Error"))
            }
        }
    }

    private fun handleProductResponse(response: Response<ProductResponse>): ResourcePagination<ProductResponse> {
        if (response.body()?.success == true) {
            response.body()?.let { resultResponse ->
                productPage++
                if (productResponse == null) {
                    productResponse = resultResponse
                } else {
                    val oldProducts = productResponse?.result?.docs
                    val newProducts = resultResponse.result.docs
                    if (newProducts != null) {
                        oldProducts?.addAll(newProducts)
                    }
                }
                return ResourcePagination.Success(productResponse ?: resultResponse)
            }
        }
        return ResourcePagination.Error(response.message())
    }
}