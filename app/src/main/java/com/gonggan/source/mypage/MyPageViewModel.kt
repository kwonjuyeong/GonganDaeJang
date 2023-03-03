package com.gonggan.source.mypage

import android.content.Context
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.modifyInfo
import java.util.*


class MyPageViewModel(userToken : String) : ViewModel() {

    private val crimeRepository = UserInfoRepository.get()

    fun onBackClicked(context: Context) {
        modifyInfo(context, userInfo?.value?.value?.password.toString())
    }

    fun onButtonClicked(context: Context) {
        modifyInfo(context, userInfo?.value?.value?.password.toString())
    }

    //userInfoDTO 참조를 갖는 속성 추가
    private var userInfo: LiveData<UserInfoDTO> ?= null

    set(userInfo) {
        field = crimeRepository.getUserInfoData()
    }


    //버튼 제목을 갖는 속성 추가
    @get:Bindable
    val id: String?
        get() = userInfo?.value?.value?.id
    @get:Bindable
    val userName: String?
        get() = userInfo?.value?.value?.user_name
    @get:Bindable
    val position: String?
        get() = userInfo?.value?.value?.user_position
    @get:Bindable
    val contact: String?
        get() = userInfo?.value?.value?.user_contact
    @get:Bindable
    val eMail: String?
        get() = userInfo?.value?.value?.user_email
    @get:Bindable
    val coName: String?
        get() = userInfo?.value?.value?.co_name
    @get:Bindable
    val coCeo: String?
        get() = userInfo?.value?.value?.co_ceo
    @get:Bindable
    val coLocation: String?
        get() = userInfo?.value?.value?.co_address
    @get:Bindable
    val coContact: String?
        get() = userInfo?.value?.value?.co_contact
    @get:Bindable
    val coType: String?
        get() = userInfo?.value?.value?.co_type
    @get:Bindable
    val coRegisnum: String?
        get() = userInfo?.value?.value?.co_regisnum
    @get:Bindable
    val authorityName: String?
        get() = userInfo?.value?.value?.authority_name

}

