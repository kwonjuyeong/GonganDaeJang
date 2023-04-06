package com.gonggan.source.Intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityIntroBinding
import com.gonggan.source.login.LoginActivity


class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

            var anim_FadeIn: Animation? = null
            var anim_ball: Animation? = null
            anim_FadeIn = AnimationUtils.loadAnimation(this, R.anim.anime_splash_fadein)
            anim_ball = AnimationUtils.loadAnimation(this, R.anim.anim_splash_ball)
            anim_FadeIn!!.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
                }
                override fun onAnimationRepeat(animation: Animation) {}
            })
            binding.introDaejang.startAnimation(anim_FadeIn)
            binding.introLogo.startAnimation(anim_FadeIn)
            binding.introGong.startAnimation(anim_ball)
            binding.introGan.startAnimation(anim_ball)
        /*
        val handler = Handler()
        handler.postDelayed(Runnable {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent) //인트로 실행 후 바로 MainActivity로 넘어감.
            finish()
        }, 1000) //1초 후 넘어감
        */
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}