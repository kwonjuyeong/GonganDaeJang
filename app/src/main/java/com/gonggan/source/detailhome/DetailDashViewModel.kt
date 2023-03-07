package com.gonggan.source.detailhome

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.SharedPreferencesManager
import com.gonggan.objects.modifyInfo
import com.gonggan.objects.moveToDash
import com.gonggan.source.mypage.UserInfoRepository


class DetailDashViewModel(application: Application) : AndroidViewModel(application){

    private val sharedPreferencesManager = SharedPreferencesManager(application)
    private val repository = UserInfoRepository(sharedPreferencesManager)

    init {
        repository.getInfo().observeForever { userInfo ->
            _userInfo.value = userInfo
        }
    }

    fun getUsers(): LiveData<UserInfoDTO?> {
        return repository.getInfo()
    }

    private val _userInfo = MutableLiveData<UserInfoDTO?>()
    val userInfo: LiveData<UserInfoDTO?> = _userInfo

    fun moveToDashBoard(context: Context, data : LiveData<UserInfoDTO?>?){
        if (data?.value != null) { // Add null-check here
            moveToDash(context, data.value!!.value.co_code,  data.value!!.value.authority_code, data.value!!.msg)
        }
    }

    fun moveToModifyUserButton(context: Context, data : LiveData<UserInfoDTO?>?){
        if (data?.value != null) {
            modifyInfo(context, data.value!!.value.password)
        }
    }

}