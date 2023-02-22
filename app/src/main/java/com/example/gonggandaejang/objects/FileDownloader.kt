package com.example.gonggandaejang.objects

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


data class DocFileDownLoadDTO(
    val consCode: String,
    val sysDocNum: String,
    val filePath: String,
    val oriName: String,
    val chaName: String
)

@SuppressLint("SimpleDateFormat")
fun loadFile(userToken: String ,imageView: ImageView, data : DocFileDownLoadDTO) {
    Thread {
        var `is`: InputStream? = null
        val handler = Handler(Looper.getMainLooper())
        try {
            //Header
            val url = URL("http://211.107.220.103:${CodeList.portNum}/commManage/docFileDownload")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json;utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("token", userToken)
            conn.setRequestProperty("sysCd", CodeList.sysCd)
            conn.connectTimeout = 1500

            //Body
            val gson = Gson()
            val inputJsonData = gson.toJson(data)
            Log.d("datasss", inputJsonData)
            val writer = OutputStreamWriter(conn.outputStream)
            if (inputJsonData != null) {
                writer.write(
                    inputJsonData.toString(),
                    0,
                    inputJsonData.toString().length
                ) // Parameter 전송
                writer.flush()
            }

            println("responseCode ${conn.responseCode}")
            println("responseCode ${conn.responseMessage}")

            // Status 가 200 일 때
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                `is` = conn.inputStream
                val bitmap = BitmapFactory.decodeStream(`is`)

                handler.postDelayed({
                    imageView.setImageBitmap(getResizedBitmap(bitmap, 1000, 1000))
                }, 0)
            }
            conn.disconnect()
        } catch (e: Exception) {
            println("An error occurred while trying to download a file.")
            e.printStackTrace()
            try {
                `is`?.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
    }.start()
}

fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
    val width = bm.width
    val height = bm.height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
}
