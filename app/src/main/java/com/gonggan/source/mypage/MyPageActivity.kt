package com.gonggan.source.mypage

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityMyPageBinding
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.SharedPreferencesManager
import com.gonggan.objects.modifyInfo
import com.gonggan.objects.moveToDash

private lateinit var backAuthState : String

class MyPageActivity : AppCompatActivity() {
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var myPageViewModel : MyPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       val binding: ActivityMyPageBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_page)

        init()

        setSupportActionBar(binding.include.mainToolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            title = getString(R.string.my_page_eng)
        }

        myPageViewModel = ViewModelProvider(this)[MyPageViewModel(this.application)::class.java]
        binding.lifecycleOwner = this
        binding.viewModel = myPageViewModel

        myPageViewModel.getUsers().observe(this) {
            binding.userIdText.text = it?.value?.id
            binding.userNameText.text = it?.value?.user_name
            binding.positionText.text = it?.value?.user_position
            binding.contactText.text = it?.value?.co_contact
            binding.eMailText.text = it?.value?.user_email
            binding.coNameText.text = it?.value?.co_name
            binding.coCeoText.text = it?.value?.co_ceo
            binding.coLocationText.text = it?.value?.co_address
            binding.coContactText.text = it?.value?.co_contact
            binding.coTypeText.text = it?.value?.co_type
            binding.coRegisnumText.text = it?.value?.co_regisnum
            binding.authorityText.text = it?.value?.authority_name
            myPageViewModel.moveToDashBoard(this, myPageViewModel.getUsers())
            myPageViewModel.moveToModifyUserButton(this, myPageViewModel.getUsers())
            backAuthState = it?.value?.authority_code.toString()
        }

    }
    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = SharedPreferencesManager(this).getString("token", "")
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
        //back pressed
        myPageViewModel.getUsers().observe(this) {
            myPageViewModel.moveToDashBoard(this, myPageViewModel.getUsers())
        }
    }
}