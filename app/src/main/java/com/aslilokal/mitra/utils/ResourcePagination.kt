package com.aslilokal.mitra.utils

sealed class ResourcePagination<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : ResourcePagination<T>(data)
    class Error<T>(message: String, data: T? = null) : ResourcePagination<T>(data, message)
    class Loading<T> : ResourcePagination<T>()
}