package com.gonggan.objects

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.example.gonggan.R
import com.gonggan.API.GetWeatherService
import com.gonggan.DTO.GetWeatherInfoDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

object CodeList {
        //Test Port Number(Port Num : 50406, 50407)
        const val portNum = "http://211.107.220.103:50406"

        //APP System Code(SY00)
        const val sysCd : String = "SY000001"

        //User Authority Code(AU00)
        //관리자, 발주처, 본사 관리자, 디자인, 시공자, 일반 사용자
        const val Buyer : String = "AU000001"
        const val company : String = "AU000002"
        const val design: String = "AU000003"
        const val work : String = "AU000004"
        const val normal : String = "AU000005"

        //Project List Code(ST00)
        const val project_all : String = "ST000000"
        const val project_ready : String = "ST000001"
        const val project_progress : String = "ST000002"
        const val project_stop : String = "ST000003"
        const val project_complete : String = "ST000004"

        //재직여부 코드
        const val Employ_yes : String = "ES000001"
        const val Employ_None : String = "ES000000"

        const val Album = 98
        const val Camera = 99
    }


