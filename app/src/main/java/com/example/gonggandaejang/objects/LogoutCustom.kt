package com.example.gonggandaejang.objects

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.allscapeservice.a22allscape_app.DTO.LogoutDTO
import com.allscapeservice.a22allscape_app.objects.callRetrofit
import com.example.gonggandaejang.API.LogoutService
import com.example.gonggandaejang.LoginActivity
import com.example.gonggandaejang.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private var logout: LogoutDTO? = null

private val retrofit = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/logout/")
private val logoutService: LogoutService = retrofit.create(LogoutService::class.java)
private lateinit var editor: SharedPreferences.Editor

//Logout 호출 함수
fun startCloseLogoutCustom(context: Context, userToken: String, sharedPreferences: SharedPreferences){
    editor = sharedPreferences.edit()
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.custom_dialog_logout)

    val text =dialog.findViewById<TextView>(R.id.logout_text)
    val logoutBtn = dialog.findViewById<Button>(R.id.logout_btn)
    val logoutNo = dialog.findViewById<Button>(R.id.logout_cancel_btn)

    text.text = "정말 로그아웃 하시겠습니까?"

    logoutBtn.setOnClickListener {
        logoutService.requestLogOut(userToken, CodeList.sysCd).enqueue(object :
            Callback<LogoutDTO> {
            override fun onFailure(call: Call<LogoutDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            override fun onResponse(call: Call<LogoutDTO>, response: Response<LogoutDTO>) {
                logout = response.body()

                if(logout?.code == 200){
                    Toast.makeText(context, "정상적으로 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    editor.clear()
                    editor.apply()
                    val intent = Intent(context , LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as Activity).finish()
                    dialog.dismiss()
                }
            }
        })
    }
    logoutNo.setOnClickListener {
        dialog.dismiss()
    }
    dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    dialog.setCanceledOnTouchOutside(true)
    dialog.setCancelable(true)
    dialog.show()
}