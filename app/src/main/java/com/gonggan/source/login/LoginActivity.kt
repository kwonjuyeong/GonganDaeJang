package com.gonggan.source.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.gonggan.databinding.ActivityLoginBinding
import com.gonggan.API.GetUserInfoService
import com.gonggan.API.LoginService
import com.gonggan.DTO.LoginDTO
import com.gonggan.DTO.LoginRequestDTO
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.objects.CodeList
import com.gonggan.objects.getSHA512
import com.gonggan.objects.moveToDash
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//로그인 화면
private const val TAG = "Login"
private var login: LoginDTO? = null
private var getUserInfo: UserInfoDTO? = null

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieAnimation.playAnimation()

        val sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        val editor = sharedPreference.edit()

        //자동 로그인 =======================================================================================================================
        val retrofitInfo = callRetrofit("${CodeList.portNum}/userManage/getMyInfo/").create(GetUserInfoService::class.java)

        if (sharedPreference.getString("userId", "").toString().isNotEmpty() && sharedPreference.getString("userPw", "").toString().isNotEmpty() && sharedPreference.getString("token", "").toString().isNotEmpty()) {
            val token = sharedPreference.getString("token", "").toString()
            retrofitInfo.requestUserInfo(token, CodeList.sysCd).enqueue(object : Callback<UserInfoDTO> {
                override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {}
                override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                    getUserInfo = response.body()
                    val gson = Gson()
                    Log.d("login_info", gson.toJson(getUserInfo?.value))

                    if (getUserInfo?.code == 200) {
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.loginLayout.visibility = GONE
                            binding.checkedLottie.playAnimation()
                            Toast.makeText(this@LoginActivity, "자동 로그인", Toast.LENGTH_SHORT).show()
                            delay(1500)
                            moveToDash(this@LoginActivity, getUserInfo?.value?.co_code.toString(), getUserInfo?.value?.authority_code.toString(), getUserInfo?.msg.toString())
                        }
                    }
                }
            })
        }
        //일반 로그인========================================================================================================================
        else {
        val loginService = callRetrofit("${CodeList.portNum}/userManage/login/").create(LoginService::class.java)

        binding.loginBtn.setOnClickListener {
            val userId = binding.loginIdEt.text.toString().trim()
            val passwd = getSHA512(binding.loginPwEt.text.toString())

            loginService.requestLogIn(CodeList.sysCd, LoginRequestDTO(userId, passwd)).enqueue(object : Callback<LoginDTO> {
                val dialog = AlertDialog.Builder(this@LoginActivity)
                override fun onFailure(call: Call<LoginDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                override fun onResponse(call: Call<LoginDTO>, response: Response<LoginDTO>) {
                    login = response.body()
                    if (login?.code == 200) {
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.loginLayout.visibility = GONE
                            binding.checkedLottie.playAnimation()
                            delay(1500)
                            retrofitInfo.requestUserInfo(login?.value?.token, CodeList.sysCd).enqueue(object : Callback<UserInfoDTO> {
                                override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {}
                                override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                                    getUserInfo = response.body()
                                    if (getUserInfo?.code == 200) {
                                        moveToDash(this@LoginActivity, getUserInfo?.value?.co_code.toString(), getUserInfo?.value?.authority_code.toString(), getUserInfo?.msg.toString())
                                    }
                                }
                            })
                        }
                        //자동로그인 체크 확인(체크 시 : userId, userPw, token 값 저장)
                        if (binding.autoLogin.isChecked) {
                            editor.putString("userId", userId)
                            editor.putString("userPw", passwd)
                            editor.putString("token", login?.value?.token)
                            editor.apply()
                        } else {
                            editor.remove("userId")
                            editor.remove("userPw")
                            editor.putString("token", login?.value?.token)
                            editor.apply()
                        }
                    }
                    //비밀번호 불일치 : PW 입력 칸 비우기
                    else if (login?.code == 453) {
                        binding.loginPwEt.text = null
                        dialog.setTitle("로그인 실패")
                        dialog.setMessage((login?.msg))
                        dialog.show()
                    }
                    //에러 : ID, PW 입력 칸 비우기
                    else {
                        binding.loginIdEt.text = null
                        binding.loginPwEt.text = null
                        dialog.setTitle("로그인 실패")
                        dialog.setMessage((login?.msg))
                        dialog.show()
                    }
                }
            })
        }
    }
    }
}