package com.hazelmobile.filetransfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hazelmobile.filetransfer.model.CrackTransfer

object Callback {

    @JvmField
    val mutableLiveData = MutableLiveData<Boolean>()
    @JvmField
    val transferMutableLiveData = MutableLiveData<CrackTransfer>()
    @JvmField
    val updateDialogMutable = MutableLiveData<Any>()
    @JvmField
    val showHotspot = MutableLiveData<String>()


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

    @JvmStatic
    fun getDialogInfo(): LiveData<Any> {
        return updateDialogMutable
    }

    @JvmStatic
    fun setDialogInfo(mObject: Any) {
        updateDialogMutable.value = mObject
    }

    @JvmStatic
    fun getHotspotName(): LiveData<String> {
        return showHotspot
    }

    @JvmStatic
    fun setHotspotName(mObject: String) {
        showHotspot.value = mObject
    }

}