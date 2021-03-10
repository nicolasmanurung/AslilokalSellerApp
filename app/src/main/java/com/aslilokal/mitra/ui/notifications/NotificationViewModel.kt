package com.aslilokal.mitra.ui.notifications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.mitra.model.data.repository.KodelapoRepository
import com.aslilokal.mitra.model.remote.response.NotificationResponse
import com.aslilokal.mitra.utils.ResourcePagination
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NotificationViewModel(private var mainRepository: KodelapoRepository) : ViewModel() {
    val notifications: MutableLiveData<ResourcePagination<NotificationResponse>> = MutableLiveData()
    var notificationResponse: NotificationResponse? = null

    suspend fun getNotifications(
        token: String,
        idSeller: String
    ) = viewModelScope.launch {
        notifications.postValue(ResourcePagination.Loading())
        try {
            val response = mainRepository.getAllNotification(token, idSeller)
            notifications.postValue(handleNotificationResponse(response))
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> notifications.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> notifications.postValue(ResourcePagination.Error("Kesalahan tak terduga"))
            }
        }
    }

    private fun handleNotificationResponse(response: Response<NotificationResponse>): ResourcePagination<NotificationResponse> {
        if (response.body()?.success == true) {
            response.body()?.let { resultNotification ->
                if (notificationResponse == null) {
                    notificationResponse = resultNotification
                } else {
                    val oldNotifications = notificationResponse?.result
                    val newNotifications = resultNotification.result
                    if (!(newNotifications.isNullOrEmpty()) && newNotifications != oldNotifications) {
                        oldNotifications?.clear()
                        oldNotifications?.addAll(newNotifications)
                    }
                }
                return ResourcePagination.Success(notificationResponse ?: resultNotification)
            }
        }
        return ResourcePagination.Error(response.message())
    }
}