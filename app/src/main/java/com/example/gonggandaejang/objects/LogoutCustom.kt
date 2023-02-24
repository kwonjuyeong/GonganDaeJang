package com.example.gonggandaejang.objects

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.airbnb.lottie.LottieAnimationView
import com.allscapeservice.a22allscape_app.DTO.LogoutDTO
import com.allscapeservice.a22allscape_app.objects.callRetrofit
import com.allscapeservice.a22allscape_app.objects.getSHA512
import com.example.gonggandaejang.*
import com.example.gonggandaejang.API.LogoutService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

private var logout: LogoutDTO? = null

private val retrofit = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/logout/")
private val logoutService: LogoutService = retrofit.create(LogoutService::class.java)
private lateinit var editor: SharedPreferences.Editor

//Logout 호출 함수
fun startCloseLogoutCustom(context: Context, userToken: String, sharedPreferences: SharedPreferences){
    editor = sharedPreferences.edit()
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.custom_dialog_logout)

    val logoutParents = dialog.findViewById<ConstraintLayout>(R.id.logout_parent_layout)
    val text =dialog.findViewById<TextView>(R.id.logout_text)
    val logoutBtn = dialog.findViewById<Button>(R.id.logout_btn)
    val logoutNo = dialog.findViewById<Button>(R.id.logout_cancel_btn)
    val logoutAnimation = dialog.findViewById<LottieAnimationView>(R.id.logout_animation)
    val logoutWaveAnimation = dialog.findViewById<LottieAnimationView>(R.id.wave_animation)
    val logoutLayout = dialog.findViewById<LinearLayout>(R.id.logout_button_layout)
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
                    CoroutineScope(Dispatchers.Main).launch {
                        logoutParents.setBackgroundColor(R.color.AllScape_blue.toInt())
                        logoutAnimation.visibility = VISIBLE
                        logoutWaveAnimation.visibility = VISIBLE
                        text.visibility = GONE
                        logoutLayout.visibility = GONE
                        logoutAnimation.playAnimation()
                        delay(200)
                        logoutWaveAnimation.playAnimation()
                        delay(1500)
                        Toast.makeText(context, "정상적으로 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                        editor.clear()
                        editor.apply()
                        val intent = Intent(context , LoginActivity::class.java)
                        context.startActivity(intent)
                        (context as Activity).finish()
                        dialog.dismiss()
                    }
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


//Logout 호출 함수
fun endCloseLogoutCustom(context: Context, userToken: String, drawer: DrawerLayout, sharedPreferences: SharedPreferences){
    editor = sharedPreferences.edit()
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.custom_dialog_logout)

    val logoutParents = dialog.findViewById<ConstraintLayout>(R.id.logout_parent_layout)
    val text =dialog.findViewById<TextView>(R.id.logout_text)
    val logoutBtn = dialog.findViewById<Button>(R.id.logout_btn)
    val logoutNo = dialog.findViewById<Button>(R.id.logout_cancel_btn)
    val logoutAnimation = dialog.findViewById<LottieAnimationView>(R.id.logout_animation)
    val logoutWaveAnimation = dialog.findViewById<LottieAnimationView>(R.id.wave_animation)
    val logoutLayout = dialog.findViewById<LinearLayout>(R.id.logout_button_layout)
    text.text = "정말 로그아웃 하시겠습니까?"
    logoutBtn.setOnClickListener {
        logoutService.requestLogOut(userToken, CodeList.sysCd).enqueue(object :
            Callback<LogoutDTO> {
            override fun onFailure(call: Call<LogoutDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            override fun onResponse(call: Call<LogoutDTO>, response: Response<LogoutDTO>) {
                logout = response.body()

                CoroutineScope(Dispatchers.Main).launch {
                    logoutParents.setBackgroundColor(R.color.AllScape_blue.toInt())
                    logoutAnimation.visibility = VISIBLE
                    logoutWaveAnimation.visibility = VISIBLE

                    text.visibility = GONE
                    logoutLayout.visibility = GONE
                    logoutAnimation.playAnimation()
                    delay(200)
                    logoutWaveAnimation.playAnimation()
                    delay(1500)
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
        drawer.closeDrawer(GravityCompat.END)
        dialog.dismiss()
    }
    dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    dialog.setCanceledOnTouchOutside(true)
    dialog.setCancelable(true)
    dialog.show()
}


//회원정보 수정 페이지 접속 비밀번호 확인
fun modifyInfo(context: Context, userPassword : String){
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.custom_dialog_modify_user)

    val text =dialog.findViewById<TextView>(R.id.modify_text)
    val modifyTitle = dialog.findViewById<TextView>(R.id.modify_title)
    val modifyBtn = dialog.findViewById<Button>(R.id.modify_btn)
    val modifyNoBtn = dialog.findViewById<Button>(R.id.modify_cancel_btn)
    val modifyPw = dialog.findViewById<EditText>(R.id.modify_password)
    val modifyAnimation = dialog.findViewById<LottieAnimationView>(R.id.modify_animation)
    text.text = "회원정보를 수정하시려면 비밀번호를 인증해주세요."
    modifyTitle.text = "비밀번호 확인"

        modifyAnimation.playAnimation()
    //비밀번호 정규식 체크(영문 소문자, 대문자, 특수문자, 숫자 최소 8글자)
    modifyPw.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(checkPW( modifyPw.text.toString().trim())){
                modifyPw.setTextColor(R.color.black.toInt())
            }else{
                modifyPw.setTextColor(-65536)
            }
        }
    })


    modifyBtn.setOnClickListener {
        val passwd = modifyPw.text.toString().trim()
        val passWd = getSHA512(passwd)
        if(passWd== userPassword){
            Toast.makeText(context, "비밀번호가 확인되었습니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(context , ModifyActivity::class.java)
            context.startActivity(intent)
            (context as Activity).finish()
            dialog.dismiss()
        }else{
            Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    modifyNoBtn.setOnClickListener {
        dialog.dismiss()
    }
    dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    dialog.setCanceledOnTouchOutside(true)
    dialog.setCancelable(true)
    dialog.show()
}

//PW 정규식
fun checkPW(userPw: String): Boolean {
    //소문자, 숫자, 특수문자 최소 1글자 , 8글자 이상
    val pwValidation = "^(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$"
    return Pattern.matches(pwValidation, userPw)
}

//email 정규식
fun checkEmail(email: String): Boolean {
    val emailValidation = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    return Pattern.matches(emailValidation, email)
}

//아이디 정규식
fun checkId(userId: String): Boolean {
    val idValidation = "^[0-9a-z.].{3,}\$"

    return Pattern.matches(idValidation, userId)
}