package com.gonggan.source.detailhome

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gonggan.R
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.SharedPreferencesManager
import com.gonggan.objects.modifyInfo
import com.gonggan.objects.moveToDash
import com.gonggan.objects.startCloseLogoutCustom
import com.gonggan.source.dailywork.DailyWatchFragment
import com.gonggan.source.mypage.OnBackPressedListener
import com.gonggan.source.mypage.UserInfoRepository
import com.gonggan.source.photogallery.PhotoGalleryFragment
import com.gonggan.source.qa.QAFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class RootViewModel(application: Application) : AndroidViewModel(application){
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

    var onBackPressedListener: OnBackPressedListener? = null
    private var userInfoLiveData: LiveData<UserInfoDTO?>? = null


    fun setUserInfoLiveData(userInfoLiveData: LiveData<UserInfoDTO?>?) {
        this.userInfoLiveData = userInfoLiveData
    }

}