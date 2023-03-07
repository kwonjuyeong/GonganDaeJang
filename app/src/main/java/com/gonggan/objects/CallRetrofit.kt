package com.gonggan.objects

//Retrofit 호출 모듈

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.example.gonggan.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.util.HashMap


object ApiUtilities{
    fun callRetrofit(url: String) : Retrofit{
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
/*
fun callRetrofit(url: String): Retrofit {
    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    return Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}*/

}

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_auto", Context.MODE_PRIVATE)

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
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

