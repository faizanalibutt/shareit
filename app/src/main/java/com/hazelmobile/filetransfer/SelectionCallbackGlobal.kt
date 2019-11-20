package com.hazelmobile.filetransfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hazelmobile.filetransfer.model.CrackTransfer

object SelectionCallbackGlobal {

    @JvmStatic
    private val mutableLiveData = MutableLiveData<Boolean>()
    @JvmStatic
    val transferMutableLiveData = MutableLiveData<CrackTransfer>()

    @JvmStatic
    fun getColor(): LiveData<Boolean> {
        return mutableLiveData
    }

    @JvmStatic
    fun setColor(select: Boolean) {
        mutableLiveData.value = select
    }

    @JvmStatic
    fun getCrackTransfer(): LiveData<CrackTransfer> {
        return transferMutableLiveData
    }

    @JvmStatic
    fun setCrackTransfer(crack: CrackTransfer) {
        transferMutableLiveData.postValue(crack)
    }

}