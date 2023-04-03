package com.gonggan.source.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gonggan.API.GetUserInfoService
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.objects.CodeList
import com.gonggan.objects.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserInfoRepository(private val sharedPreferencesManager: SharedPreferencesManager){

    private val infoApiService: GetUserInfoService = callRetrofit("${CodeList.portNum}/userManage/getMyInfo/").create(GetUserInfoService::class.java)

    fun getMyToken(): String {
        return sharedPreferencesManager.getString("token", "")
    }

    fun getInfo(): LiveData<UserInfoDTO?> {
        val data = MutableLiveData<UserInfoDTO>()
        infoApiService.requestUserInfo(getMyToken(),CodeList.sysCd).enqueue(object : Callback<UserInfoDTO>{
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                data.value = response.body()
            }
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {
            }
        })
        return data
    }

}
