package com.example.gonggandaejang

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.allscapeservice.a22allscape_app.DTO.LoginDTO
import com.allscapeservice.a22allscape_app.DTO.LoginRequestDTO
import com.allscapeservice.a22allscape_app.DTO.UserInfoDTO
import com.allscapeservice.a22allscape_app.objects.callRetrofit
import com.allscapeservice.a22allscape_app.objects.getSHA512
import com.example.gonggandaejang.API.GetUserInfoService
import com.example.gonggandaejang.API.LoginService
import com.example.gonggandaejang.databinding.ActivityLoginBinding
import com.example.gonggandaejang.objects.CodeList
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//로그인 화면

private var login: LoginDTO? = null
private var getUserInfo: UserInfoDTO? = null

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieAnimation.playAnimation()

        val sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        val editor = sharedPreference.edit()

        val retrofitInfo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/getMyInfo/")
        val getUserInfoService: GetUserInfoService = retrofitInfo.create(GetUserInfoService::class.java)


        //자동 로그인 ver2========================================================================================================================
        if (sharedPreference.getString("userId", "").toString().isNotEmpty() && sharedPreference.getString("userPw", "").toString().isNotEmpty() && sharedPreference.getString("token", "").toString().isNotEmpty()) {
            val token = sharedPreference.getString("token", "").toString()
            getUserInfoService.requestUserInfo(token, CodeList.sysCd).enqueue(object : Callback<UserInfoDTO> {
                override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {}
                override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                    getUserInfo = response.body()
                    val gson = Gson()
                    Log.d("login_info", gson.toJson(getUserInfo?.value))
                    Toast.makeText(this@LoginActivity, "자동 로그인", Toast.LENGTH_SHORT).show()
                    if (getUserInfo?.code == 200) {
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.loginLayout.visibility = GONE
                            binding.checkedLottie.playAnimation()
                            delay(1500)
                            when (getUserInfo?.value?.authority_code) {
                                CodeList.Buyer -> {
                                    val intent =
                                        Intent(this@LoginActivity, DashboardEnterprise::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                CodeList.design, CodeList.company, CodeList.work -> {
                                    val intent =
                                        Intent(this@LoginActivity, DashboardUsers::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                else -> {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        getUserInfo?.msg,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            })
        }
        //일반 로그인========================================================================================================================
        else {
            val retrofit = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/login/")
            val loginService: LoginService = retrofit.create(LoginService::class.java)

            binding.loginBtn.setOnClickListener {

                val userId = binding.loginIdEt.text.toString().trim()
                val passWd = binding.loginPwEt.text.toString()
                val passwd = getSHA512(passWd)
                val loginDTO = LoginRequestDTO(userId, passwd)

                loginService.requestLogIn(CodeList.sysCd, loginDTO).enqueue(object : Callback<LoginDTO> {
                    val dialog = AlertDialog.Builder(this@LoginActivity)
                    override fun onFailure(call: Call<LoginDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                    override fun onResponse(call: Call<LoginDTO>, response: Response<LoginDTO>) {
                        login = response.body()
                        Log.d("login_service", login?.code.toString())
                        Log.d("login_service", login?.msg.toString())
                        Log.d("login_service", login?.value.toString())
                        if (login?.code == 200) {
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.loginLayout.visibility = GONE
                                binding.checkedLottie.playAnimation()
                                delay(1500)
                                getUserInfoService.requestUserInfo(login?.value?.token, CodeList.sysCd).enqueue(object : Callback<UserInfoDTO> {
                                    override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {}
                                    override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                                        getUserInfo = response.body()
                                        val gson = Gson()
                                        Log.d("login_info", gson.toJson(getUserInfo?.value))
                                        if (getUserInfo?.code == 200) {
                                            when (getUserInfo?.value?.authority_code) {
                                                CodeList.Buyer -> {
                                                    val intent = Intent(this@LoginActivity, DashboardEnterprise::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                                CodeList.design, CodeList.company, CodeList.work -> {
                                                    val intent = Intent(this@LoginActivity, DashboardUsers::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                                else -> {
                                                    Toast.makeText(this@LoginActivity, getUserInfo?.msg, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                })
                            }
                            Toast.makeText(this@LoginActivity, "정상적으로 로그인되었습니다.", Toast.LENGTH_SHORT).show()

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