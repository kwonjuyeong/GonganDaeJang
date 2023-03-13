package com.gonggan.source.mypage

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.SharedPreferencesManager
import com.gonggan.objects.modifyInfo
import com.gonggan.objects.moveToDash

class MyPageViewModel(application: Application) : AndroidViewModel(application){

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

    fun moveToDashBoard(context: Context, data: LiveData<UserInfoDTO?>?){
        if (data?.value != null) { // Add null-check here
            moveToDash(context, data.value!!.value.co_code,  data.value!!.value.authority_code, data.value!!.msg)
        }

    }

    fun moveToModifyUserButton(context: Context, data : LiveData<UserInfoDTO?>?){
        if (data?.value != null) {
            modifyInfo(context, data.value!!.value.password)
        }
    }

    val id: String?
        get() = getUsers().value?.value?.id
    val userName: String?
        get() = getUsers().value?.value?.user_name
    val position: String?
        get() = getUsers().value?.value?.user_position
    val contact: String?
        get() = getUsers().value?.value?.user_contact
    val eMail: String?
        get() = getUsers().value?.value?.user_email
    val coName: String?
        get() = getUsers().value?.value?.co_name
    val coCeo: String?
        get() = getUsers().value?.value?.co_ceo
    val coLocation: String?
        get() = getUsers().value?.value?.co_address
    val coContact: String?
        get() = getUsers().value?.value?.co_contact
    val coType: String?
        get() = getUsers().value?.value?.co_type
    val coRegisnum: String?
        get() = getUsers().value?.value?.co_regisnum
    val authorityName: String?
        get() = getUsers().value?.value?.authority_name
}



