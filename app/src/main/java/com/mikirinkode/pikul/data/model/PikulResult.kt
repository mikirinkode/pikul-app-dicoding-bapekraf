package com.mikirinkode.pikul.data.model

// "Kelas untuk menyimpan status pengambilan data"

sealed class PikulResult<out R> private constructor(){
    data class Success<out T>(val data: T): PikulResult<T>()
    data class Error(val errorMessage: String): PikulResult<Nothing>()
    data class LoadingWithProgress(val progress: Int): PikulResult<Nothing>()
    object Loading: PikulResult<Nothing>()
}