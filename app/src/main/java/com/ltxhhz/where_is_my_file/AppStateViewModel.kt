package com.ltxhhz.where_is_my_file

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppStateViewModel : ViewModel() {
    private val _list = MutableStateFlow(listOf<ReceiveFile>())
    val list: StateFlow<List<ReceiveFile>> = _list

    fun clearList() {
        _list.value = emptyList()
    }

    fun addItem(item: ReceiveFile) {
        _list.value += item
    }
}