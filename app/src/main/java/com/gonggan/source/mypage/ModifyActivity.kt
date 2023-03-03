package com.gonggan.source.mypage

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityModifyBinding
import com.gonggan.API.GetCoListService
import com.gonggan.API.GetCodeListService
import com.gonggan.API.GetUserInfoService
import com.gonggan.API.ModifyUserService
import com.gonggan.Adapter.CoListAdapter
import com.gonggan.Adapter.JoinCoListData
import com.gonggan.DTO.*
import com.gonggan.objects.*
import com.gonggan.objects.CodeList.sysCd
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private var getUserInfo: UserInfoDTO? = null
private var getCo: GetCoListDTO? = null
private var code : GetCodeListDTO? = null
private var modify : ModifyUserDTO? = null

class ModifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModifyBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String

    private val coListData = arrayListOf<JoinCoListData>()
    private var passwdState = 0

    //공종 Spinner item
    private var authList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        supportActionBar?.title = getString(R.string.modify_info)

        //사용자 정보 표시==================================================================================================================================
        val retrofitUserInfo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/getMyInfo/")
        val userInfoService: GetUserInfoService = retrofitUserInfo.create(GetUserInfoService::class.java)
        //Code 불러오기============================================================================================================
        val authSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, authList)
        binding.authoritySpinner.adapter =  authSpinner

        val codeList = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/getCodeList/{reqType}/")
        val codeListService: GetCodeListService = codeList.create(GetCodeListService::class.java)

        codeListService.requestGetCodeList("AU00", sysCd).enqueue(object :
            Callback<GetCodeListDTO> {
            override fun onFailure(call: Call<GetCodeListDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            override fun onResponse(call: Call<GetCodeListDTO>, response: Response<GetCodeListDTO>) {
                code = response.body()
                for (i in 0 until code?.value?.size!!) {
                    authList.add(code?.value?.get(i)?.subcode_name.toString())
                }
                authSpinner.notifyDataSetChanged()
                userInfoService.requestUserInfo(userToken, sysCd).enqueue(object :
                    Callback<UserInfoDTO> {
                    override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                        getUserInfo = response.body()
                        Log.d("ddddd", Gson().toJson(getUserInfo?.value))

                        //Response code 200 : 통신성공
                        if (getUserInfo?.code == 200) {
                            binding.userIdText.text = getUserInfo?.value?.id
                            binding.userNameText.text = getUserInfo?.value?.user_name
                            binding.userPositionText.setText(getUserInfo?.value?.user_position)
                            binding.userContactText.setText(getUserInfo?.value?.user_contact)
                            binding.userEmailText.setText(getUserInfo?.value?.user_email)
                            binding.coName.setText(getUserInfo?.value?.co_name)
                            binding.coCEOText.setText(getUserInfo?.value?.co_ceo)
                            binding.coCode.text = getUserInfo?.value?.co_code
                            binding.coLocationText.setText(getUserInfo?.value?.co_address)
                            binding.coContactText.setText(getUserInfo?.value?.co_contact)
                            binding.coTypeText.setText(getUserInfo?.value?.co_type)
                            binding.coRegisnumText.setText(getUserInfo?.value?.co_regisnum)
                            binding.authoritySpinnerText.text = getUserInfo?.value?.authority_name

                            binding.coCEOText.isEnabled = false
                            binding.coLocationText.isEnabled = false
                            binding.coContactText.isEnabled = false
                            binding.coRegisnumText.isEnabled = false
                            binding.coTypeText.isEnabled = false
                        }
                    }
                })
            }
        })

        //password 변경 or 그대로=======================================================================================================================
        binding.passwdBtn.setOnClickListener {
            if(passwdState == 0){
                passwdState = 1
                binding.modifyUserBtn.isEnabled = false
                binding.passwdBtn.text = "변경 안함"
                binding.userPwLayout.visibility = VISIBLE
                binding.passwdLayoutHint.visibility = VISIBLE
                binding.userPwCheckLayout.visibility = VISIBLE

                //비밀번호 정규식 체크(영문 소문자, 대문자, 특수문자, 숫자 최소 8글자)
                binding.userPw.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if(checkPW( binding.userPw.text.toString().trim())){
                            binding.userPw.setTextColor(R.color.black.toInt())
                            binding.userPwCheck.isEnabled = true
                            binding.checkPasswordBtn.isEnabled = true
                        }else{
                            binding.userPw.setTextColor(-65536)
                            binding.userPwCheck.isEnabled = false
                            binding.checkPasswordBtn.isEnabled = false
                        }
                    }
                })

                //비밀번호 확인
                binding.checkPasswordBtn.setOnClickListener {
                    if(binding.userPw.text.toString() == binding.userPwCheck.text.toString()){
                        Toast.makeText(this@ModifyActivity, "비밀번호가 일치합니다.", Toast.LENGTH_SHORT).show()
                        binding.modifyUserBtn.isEnabled = true
                    }
                    else{
                        Toast.makeText(this@ModifyActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                        binding.modifyUserBtn.isEnabled = false
                    }
                }
                //비밀번호 확인 받은 후 변경 시 다시 확인 받아야함
                binding.userPwCheck.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if(binding.userPw.text != binding.userPwCheck.text){
                            binding.modifyUserBtn.isEnabled = false
                        }
                    }
                })


            }else if(passwdState == 1){
                passwdState = 0
                binding.passwdBtn.text = "패스워드 변경"
                binding.userPwLayout.visibility = GONE
                binding.passwdLayoutHint.visibility = GONE
                binding.userPwCheckLayout.visibility = GONE

                binding.userPwCheck.setText("")
                binding.userPw.setText("")
                binding.modifyUserBtn.isEnabled = true
            }
        }

        //입력정보 하이픈(-) 처리 및 유효성 검사===========================================================================================================
        //사용자 핸드폰 하이픈(-)
        binding.userContactText.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        //회사 연락처 하이픈(-)
        binding.coContactText.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        //사업자 등록번호 하이픈(-)
        binding.coRegisnumText.addTextChangedListener(object : TextWatcher {
            var prevL = 0
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                prevL = binding.coRegisnumText.text.toString().length
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val length = s.length
                if (prevL < length && (length == 3 || length == 6))  {
                    val data: String = binding.coRegisnumText.text.toString()
                    binding.coRegisnumText.setText(getString(R.string.join_resinum_format, data))
                    binding.coRegisnumText.setSelection(length + 1)
                }
            }
        })

        //이메일 정규식 체크(xxxxx@xxxxx.com)
        binding.userEmailText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(checkEmail(binding.userEmailText.text.toString().trim())){
                    binding.userEmailText.setTextColor(R.color.black.toInt())
                }else{
                    binding.userEmailText.setTextColor(-65536)
                }
            }
        })


        //Spinner Selected
        binding.authoritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                when (position) {
                    position -> {
                        binding.authoritySpinnerText.text = binding.authoritySpinner.selectedItem.toString()
                    }
                }
            }
        }

        //회사찾기 클릭 시 현재 회사 정보 띄어주고 회사정보 찾기 데이터들만 보이게
        binding.searchBtn.setOnClickListener {
            binding.coName.setText(getUserInfo?.value?.co_name)
            binding.coCEOText.setText(getUserInfo?.value?.co_ceo)
            binding.coCode.text = getUserInfo?.value?.co_code
            binding.coLocationText.setText(getUserInfo?.value?.co_address)
            binding.coContactText.setText(getUserInfo?.value?.co_contact)
            binding.coTypeText.setText(getUserInfo?.value?.co_type)
            binding.userPositionText.setText(getUserInfo?.value?.user_position)
            binding.coRegisnumText.setText(getUserInfo?.value?.co_regisnum)
            binding.authoritySpinnerText.text = getUserInfo?.value?.authority_name
            binding.joinSearchCompanyInfo.visibility = VISIBLE
            binding.coListRecycler.visibility = VISIBLE
            binding.coNameLayout.visibility = VISIBLE
            binding.coContactLayout.visibility = VISIBLE
            binding.coCeoLayout.visibility = VISIBLE
            binding.coLocationLayout.visibility = VISIBLE
            binding.coRegisnumLayout.visibility = VISIBLE
            binding.userPositionLayout.visibility = VISIBLE
            binding.authorityLayout.visibility = VISIBLE
            binding.coTypeLayout.visibility = VISIBLE

            binding.coCEOText.isEnabled = false
            binding.coLocationText.isEnabled = false
            binding.coContactText.isEnabled = false
            binding.coRegisnumText.isEnabled = false
            binding.coTypeText.isEnabled = false
        }
        //회사 직접입력일 경우 데이터 지워주고
        binding.realInputBtn.setOnClickListener {
            //회사 이름
            binding.coName.setText("")
            //회사 대표자
            binding.coCEOText.setText("")
            //업종
            binding.coTypeText.setText("")
            binding.coCode.text = ""
            //회사 소재지
            binding.coLocationText.setText("")
            //회사 전화번호
            binding.coContactText.setText("")
            //사업자 등록번호
            binding.coRegisnumText.setText("")
            binding.userPositionText.setText("")
            binding.authoritySpinnerText.text = ""
            coListData.clear()
            binding.joinSearchCompanyInfo.visibility =GONE
            binding.coListRecycler.visibility = GONE
            binding.coNameLayout.visibility = VISIBLE
            binding.coContactLayout.visibility = VISIBLE
            binding.coCeoLayout.visibility = VISIBLE
            binding.coLocationLayout.visibility = VISIBLE
            binding.coRegisnumLayout.visibility = VISIBLE
            binding.userPositionLayout.visibility = VISIBLE
            binding.authorityLayout.visibility = VISIBLE
            binding.coTypeLayout.visibility = VISIBLE
            binding.coCEOText.isEnabled = true
            binding.coLocationText.isEnabled = true
            binding.coContactText.isEnabled = true
            binding.coRegisnumText.isEnabled = true
            binding.coTypeText.isEnabled = true
        }
        binding.outCompanyBtn.setOnClickListener {
            //회사 이름
            binding.coName.setText("")
            //회사 대표자
            binding.coCEOText.setText("")
            binding.coCode.text = ""
            //업종
            binding.coTypeText.setText("")
            //회사 소재지
            binding.coLocationText.setText("")
            //회사 전화번호
            binding.coContactText.setText("")
            //사업자 등록번호
            binding.coRegisnumText.setText("")
            binding.userPositionText.setText("")
            binding.authoritySpinnerText.text = ""
            coListData.clear()
            binding.joinSearchCompanyInfo.visibility = GONE
            binding.coListRecycler.visibility = GONE
            binding.coNameLayout.visibility = GONE
            binding.coListRecycler.visibility = GONE
            binding.coContactLayout.visibility = GONE
            binding.coCeoLayout.visibility = GONE
            binding.coLocationLayout.visibility = GONE
            binding.coRegisnumLayout.visibility = GONE
            binding.userPositionLayout.visibility = GONE
            binding.authorityLayout.visibility = GONE
            binding.coTypeLayout.visibility = GONE
        }

        //회사리스트 조회===========================================================================================================================
        val retrofitCo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/getCoList/ALL/")
        val getCoListService: GetCoListService = retrofitCo.create(GetCoListService::class.java)
        //co_list
        binding.coListRecycler.layoutManager = LinearLayoutManager(this)
        binding.coListRecycler.adapter = CoListAdapter(coListData, onClickSelect = { selectedTask(it) })

        binding.joinSearchCompanyInfo.setOnClickListener {
            binding.coListRecycler.visibility = VISIBLE
            coListData.clear()
            val coInputName = binding.coName.text.toString()

            getCoListService.requestGetCoList(sysCd, GetCoListRequestDTO(coInputName))
                .enqueue(object : Callback<GetCoListDTO> {
                    override fun onFailure(call: Call<GetCoListDTO>, t: Throwable) {
                        Log.d("retrofit", t.toString())
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(
                        call: Call<GetCoListDTO>,
                        response: Response<GetCoListDTO>
                    ) {
                        getCo = response.body()

                        if (getCo?.code == 200) {
                            val len = getCo?.value?.size
                            for (i in 0 until len!!) {
                                val coAddress = getCo?.value?.get(i)?.co_address.toString()
                                val coCeo = getCo?.value?.get(i)?.co_ceo.toString()
                                val coCode = getCo?.value?.get(i)?.co_code.toString()
                                val coContact = getCo?.value?.get(i)?.co_contact.toString()
                                val coName = getCo?.value?.get(i)?.co_name.toString()
                                val coType = getCo?.value?.get(i)?.co_type.toString()

                                val coListRecyclerData = JoinCoListData(coAddress, coCeo, coCode, coContact, coName, coType
                                )
                                coListData.add(coListRecyclerData)
                            }
                            binding.coListRecycler.adapter?.notifyDataSetChanged()
                        }

                    }
                })
        }


        val retrofitModify = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/modifyUser/")
        val modifyUserService: ModifyUserService = retrofitModify.create(ModifyUserService::class.java)

        //회원정보수정 버튼
        binding.modifyUserBtn.setOnClickListener {

            val userId = binding.userIdText.text.toString().trim()
            val authorityName = binding.authoritySpinnerText.text.toString()
            var authorityCode  = ""
            for(i in 0 until code?.value?.size!!){
                if(code?.value?.get(i)?.subcode_name.toString() == authorityName)
                {
                    authorityCode = code?.value?.get(i)?.fullcode.toString()
                }
            }

            var userPw = ""

            if(passwdState == 0){
                userPw = getUserInfo?.value?.password.toString()
            }else if(passwdState == 1){
                val password = binding.userPw.text.toString().trim()
                userPw = getSHA512(password)
            }

                val userName = getUserInfo?.value?.user_name.toString()
                val useType = getUserInfo?.value?.use_type.toString()
                val userState = getUserInfo?.value?.user_state.toString()
                val userPosition = binding.userPositionText.text.toString().trim()
                val userContact = binding.userContactText.text.toString().trim()
                val userEmail = binding.userEmailText.text.toString().trim()
                val coName = binding.coName.text.toString().trim()
                val coCeo = binding.coCEOText.text.toString().trim()
                val coCode = binding.coCode.text.toString().trim()
                val coType = binding.coTypeText.text.toString().trim()
                val coAddress = binding.coLocationText.text.toString().trim()
                val coContact = binding.coContactText.text.toString().trim()
                val coResNum = binding.coRegisnumText.text.toString().trim()
            val listFieldRating = ArrayList<HashMap<String, String>>()

            val jsonObject = JSONObject("{\"user_regisnum\":\"\", \"id\":\"${userId}\",\"authority_code\":\"${authorityCode}\",\"password\":\"${userPw}\",\"user_state\":\"${userState}\",\"use_type\":\"${useType}\",\"user_name\":\"${userName}\",\"user_position\":\"${userPosition}\",\"user_contact\":\"${userContact}\",\"user_email\":\"${userEmail}\",\"co_name\":\"${coName}\",\"co_ceo\":\"${coCeo}\",\"co_type\":\"${coType}\",\"co_code\":\"${coCode}\",\"co_address\":\"${coAddress}\",\"co_contact\":\"${coContact}\",\"co_regisnum\":\"${coResNum}\",\"regisnum\":\"\",\"user_type\":\"\",\"employ_status\":\"\",\"field_rating\": $listFieldRating}").toString()
            val jsonBody = RequestBody.create(MediaType.parse("application/json"), jsonObject)
            Log.d("json", jsonObject)

            //val inputData = ModifyRequestDTo(userId, authorityCode, authorityName, userPw,userState,userStateName, useType, userName, userPosition, userContact, userEmail,coAddress, coCeo,coContact, coName,coResNum,coType)

                //modifyUser(회원수정) API 호출
                modifyUserService.requestModify(sysCd, userToken, jsonBody).enqueue(object : Callback<ModifyUserDTO> {
                    override fun onFailure(call: Call<ModifyUserDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                    override fun onResponse(call: Call<ModifyUserDTO>, response: Response<ModifyUserDTO>) {
                        modify = response.body()
                        Log.d("modify", modify?.code.toString())
                        Log.d("modify", modify?.msg.toString())
                        if(modify?.code == 200){
                            Log.d("modify_success", modify?.code.toString())
                            Log.d("modify_success", modify?.msg.toString())
                            editor.clear()
                            //shared 삭제된 사용자 토큰값 다시 저장
                            editor.putString("token", userToken)
                            editor.putString("userId", userId)
                            editor.putString("userPw", userPw)
                            editor.apply()
                            Toast.makeText(this@ModifyActivity, "회원정보 수정 성공", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ModifyActivity, MyPageActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else{
                            Log.d("modify_fail", modify?.code.toString())
                            Log.d("modify_fail", modify?.msg.toString())
                            Toast.makeText(this@ModifyActivity, "회원정보수정 실패 : ${modify?.msg.toString()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }


    }

    //회사 선택 버튼
    @SuppressLint("NotifyDataSetChanged")
    private fun selectedTask(data: JoinCoListData) {
        binding.coListRecycler.visibility = GONE

        binding.coLocationText.setText(data.co_address)
        binding.coCEOText.setText(data.co_ceo)
        binding.coTypeText.setText(data.co_type)
        binding.coContactText.setText(data.co_contact)
        binding.coName.setText(data.co_name)
        binding.coCode.text = data.co_code
    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
    super.onBackPressed()
    val intent = Intent(this@ModifyActivity, MyPageActivity::class.java)
    startActivity(intent)
    finish()
    }

}