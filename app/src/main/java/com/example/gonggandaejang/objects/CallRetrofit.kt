package com.allscapeservice.a22allscape_app.objects

//Retrofit 호출 모듈

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

fun callRetrofit(url: String): Retrofit {
    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    return Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

//Log 길이 
fun logLineBreak(str: String) {
    if (str.length > 3000) {    // 텍스트가 3000자 이상이 넘어가면 줄
        Log.i("e", str.substring(0, 3000))
        logLineBreak(str.substring(3000))
    } else {
        Log.i("e", str)
    }
}
