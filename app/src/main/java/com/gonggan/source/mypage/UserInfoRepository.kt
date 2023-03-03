package com.gonggan.source.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gonggan.API.GetUserInfoService
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.CodeList
import com.gonggan.objects.callRetrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserInfoRepository{

    val retrofitUserInfo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/getMyInfo/").create(GetUserInfoService::class.java)

    fun getUserInfoData(userToken : String): LiveData<UserInfoDTO> {
        val data = MutableLiveData<UserInfoDTO>()
        retrofitUserInfo.requestUserInfo(userToken, CodeList.sysCd).enqueue(object : Callback<UserInfoDTO> {
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {

            }
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) { t.stackTrace }
        })
        return data
    }

    companion object{
        private var INSTANCE : UserInfoRepository ?= null

        fun initialize(){
            if(INSTANCE == null){
                INSTANCE = UserInfoRepository()
            }
        }
        fun get() : UserInfoRepository{
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}