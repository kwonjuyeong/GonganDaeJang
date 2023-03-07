package com.gonggan.source.detailhome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gonggan.API.GetUserInfoService
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.ApiUtilities
import com.gonggan.objects.CodeList
import com.gonggan.objects.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DetailDashRepository(private val sharedPreferencesManager: SharedPreferencesManager){

    private val infoApiService: GetUserInfoService = ApiUtilities.callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/getMyInfo/")
        .create(GetUserInfoService::class.java)

    fun getMyToken(): String {
        return sharedPreferencesManager.getString("token", "")
    }

    fun getInfo(): LiveData<UserInfoDTO?> {
        val data = MutableLiveData<UserInfoDTO>()
        infoApiService.requestUserInfo(getMyToken(), CodeList.sysCd).enqueue(object :
            Callback<UserInfoDTO> {
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                data.value = response.body()
            }
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {
            }
        })
        return data
    }

}
