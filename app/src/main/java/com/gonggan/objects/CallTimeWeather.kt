package com.gonggan.objects

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.example.gonggan.R
import com.gonggan.API.GetCurTimeInfoService
import com.gonggan.API.GetWeatherService
import com.gonggan.DTO.GetCurTimeInfoDTO
import com.gonggan.DTO.GetWeatherInfoDTO
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

private var weather: GetWeatherInfoDTO? = null

fun callWeatherInfo(userToken : String, context: Context, weatherImageView: ImageView, weatherTextView: TextView){

    val getWeatherService = ApiUtilities.callRetrofit("${CodeList.portNum}/commManage/getWeatherInfo/").create(GetWeatherService::class.java)
    getWeatherService.requestWeather(CodeList.sysCd, userToken).enqueue(object :
        Callback<GetWeatherInfoDTO> {
        override fun onFailure(call: Call<GetWeatherInfoDTO>, t: Throwable) { Log.d("retrofit_weather", t.toString()) }
        override fun onResponse(call: Call<GetWeatherInfoDTO>, response: Response<GetWeatherInfoDTO>) {
            weather = response.body()

            val ptyResult = weather?.value?.ptyResult
            val skyResult = weather?.value?.skyResult
            val t1kResult = weather?.value?.t1hResult
            if (weather?.code == 200) {
                val weather = weatherCode(ptyResult, skyResult, weatherImageView)
                val ptyReturn = weather["pty"]
                val skyReturn = weather["sky"]
                if(ptyReturn == "") {weatherTextView.text = "$skyReturn"
                }else{
                    weatherTextView.text = context.getString(R.string.weather_format, t1kResult, ptyReturn, skyReturn)
                }
            }
        }
    })
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

private var curTime: GetCurTimeInfoDTO? = null
private var job : Job?= null

fun callTimeSet(userToken: String, timeTextView: TextView) : Job{
    job = CoroutineScope(Dispatchers.IO).launch {
        val getCurTimeInfoService = ApiUtilities.callRetrofit("${CodeList.portNum}/commManage/getCurTimeInfo/").create(GetCurTimeInfoService::class.java)
        while (true) {
            getCurTimeInfoService.requestCurTime(CodeList.sysCd, userToken).enqueue(object :
                Callback<GetCurTimeInfoDTO> {
                override fun onFailure(call: Call<GetCurTimeInfoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                override fun onResponse(call: Call<GetCurTimeInfoDTO>, response: Response<GetCurTimeInfoDTO>) {
                    curTime = response.body()
                    if (curTime?.code == 200) {
                        timeTextView.text = curTime?.value
                    }
                }
            })
            delay(1000)
            Log.d("timeCalled", curTime?.value.toString())
        }
    }
    return job as Job
}