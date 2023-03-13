package com.gonggan.objects

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.gonggan.source.dashboard.DashNormal
import com.gonggan.source.dashboard.DashboardEnterprise
import com.gonggan.source.dashboard.DashboardUsers

fun moveToDash(context: Context, coCode : String, authCode : String, errorMsg : String){
        if(coCode.isNotEmpty()){
        when (authCode) {
            CodeList.Buyer -> {
                val intent = Intent(context, DashboardEnterprise::class.java)
                context.startActivity(intent)
                (context as Activity).finish()
            }
            CodeList.design, CodeList.company, CodeList.work -> {
                val intent = Intent(context, DashboardUsers::class.java)
                context.startActivity(intent)
                (context as Activity).finish()
            }
            CodeList.normal -> {
                val intent = Intent(context, DashNormal::class.java)
                context.startActivity(intent)
                (context as Activity).finish()
            }
            else -> {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
        }else{
            val intent = Intent(context, DashNormal::class.java)
            context.startActivity(intent)
            (context as Activity).finish()
        }
 }
