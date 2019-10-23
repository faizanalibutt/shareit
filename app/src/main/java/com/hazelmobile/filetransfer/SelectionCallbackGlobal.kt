package com.hazelmobile.filetransfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object SelectionCallbackGlobal {

    @JvmStatic
    private val mutableLiveData = MutableLiveData<Boolean>()

    @JvmStatic
    fun getColor(): LiveData<Boolean> {
        return mutableLiveData
    }

    @JvmStatic
    fun setColor(select: Boolean) {
        mutableLiveData.value = select
    }

}