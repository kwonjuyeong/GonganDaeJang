package com.gonggan.objects

//Retrofit 호출 모듈

import android.util.Log
import android.widget.ImageView
import com.example.gonggan.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.HashMap

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

//날씨 API 코드
fun weatherCode(ptyResult : String?, skyResult : String?, imageView: ImageView) : HashMap<String, String> {

    val weather = HashMap<String, String>()
    var ptyReturn = ""
    var skyReturn = ""

    when (ptyResult) {
        "0" -> ptyReturn = ""
        "1" -> ptyReturn = "비"
        "2" -> ptyReturn = "비/눈"
        "3" -> ptyReturn = "눈"
        "5" -> ptyReturn = "빗방울"
        "6" -> ptyReturn = "빗방울/눈날림"
        "7" -> ptyReturn = "눈날림"
    }
    when (skyResult) {
        "1" -> {
            skyReturn = "맑음"
            imageView.setImageResource(R.drawable.ic_sunny)
        }
        "3" -> {
            skyReturn = "구름 많음"
            imageView.setImageResource(R.drawable.ic_cloud)
        }
        "4" -> {
            skyReturn = "흐림"
            imageView.setImageResource(R.drawable.ic_little_cloud)
        }
    }
    weather["pty"] = ptyReturn
    weather["sky"] = skyReturn

    Log.d("weather", weather.toString())
    return weather
}

