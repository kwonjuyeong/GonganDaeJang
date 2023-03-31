package com.gonggan.objects

//Retrofit 호출 모듈

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.example.gonggan.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*


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



fun callSelectCalendar(text : TextView, context: Context){
    text.text = ""
    val cal = Calendar.getInstance()
    val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        text.text = context.getString(R.string.calender_day_format, year.toString(), getMonth(month), getDay(dayOfMonth))
    }
    Log.d("clicked", "clicked")
    DatePickerDialog(context, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(
        Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = Date().time }.show()
}
