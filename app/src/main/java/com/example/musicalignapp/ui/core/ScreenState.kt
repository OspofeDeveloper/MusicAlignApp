package com.example.musicalignapp.ui.core

sealed class ScreenState<T> {
    class Loading<T> : ScreenState<T>()
    class Error<T>(val error: String) : ScreenState<T>()
    class Success<T>(val data: T) : ScreenState<T>()
}