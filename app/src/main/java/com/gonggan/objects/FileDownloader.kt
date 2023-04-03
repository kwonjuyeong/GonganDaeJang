package com.gonggan.objects

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.text.SimpleDateFormat

data class DocFileDownLoadDTO(
    val consCode: String,
    val sysDocNum: String,
    val filePath: String,
    val oriName: String,
    val chaName: String
)
//3.1.5 문서 파일 다운로드
@SuppressLint("SimpleDateFormat")
fun docFileDownload(context: Context, userToken : String, data : DocFileDownLoadDTO){
        val handler = Handler(Looper.getMainLooper())
        val spec = "${CodeList.portNum}/commManage/docFileDownload"
        val outputDir = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DOWNLOADS

        val currentTime = System.currentTimeMillis()
        val dataFormat = SimpleDateFormat("yyyyMMdd-hhmmss")
        val plus = dataFormat.format(currentTime)

        var `is`: InputStream? = null
        var os: FileOutputStream? = null
        try {
            //Header
            val url = URL(spec)
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
                handler.postDelayed({
                    Toast.makeText(context, "파일을 다운로드하고 있습니다...", Toast.LENGTH_SHORT).show()
                }, 0)

                var fileName = ""
                val disposition = conn.getHeaderField("Content-Disposition")
                // val contentType = conn.contentType

                if (disposition != null) {
                    Log.d("filename_disposition", disposition.toString())
                    val target: String = if (disposition.contains("filename*=")) {
                        "filename*="
                    } else {
                        "filename="
                    }
                    //val target1 = "filename="
                    val index = disposition.indexOf(target)

                    if (index != -1) {
                        fileName = disposition.substring(index + target.length)
                        fileName = fileName.replace("\"", "") //(앞뒤 따옴표 제거)
                        fileName = fileName.replace("UTF-8''", "") //(앞뒤 따옴표 제거)
                        val pattern = Regex("\\d{8}-\\d{6}_")
                        fileName = pattern.replace(fileName, "")
                    }
                }

                Log.d("filenames", fileName)

                val file: File = if (fileName.contains(".jpg") || fileName.contains(".png") || fileName.contains(
                            ".pdf"
                        ) || fileName.contains(".jpeg")
                    ) {
                        File(outputDir, URLDecoder.decode(plus + "_" + fileName, "utf-8"))
                    } else {
                        File(outputDir, URLDecoder.decode(plus + "_" + fileName + ".jpg", "utf-8"))
                    }

                Log.d("filename", fileName)
                Log.d("filename2", file.name.toString())

                //서버에서 데이터를 읽어옴
                `is` = conn.inputStream
                //읽어온 데이터를 다운로드 폴더에 출력함
                os = FileOutputStream(file)

                val bufferSize = 4096
                var bytesRead: Int
                val buffer = ByteArray(bufferSize)

                while ((`is`.read(buffer).also { bytesRead = it }) != -1) {
                    os.write(buffer, 0, bytesRead)
                }

                os.close()
                `is`.close()

                println("File downloaded")
                handler.postDelayed({
                    Toast.makeText(context, "${file.name}파일 다운로드 성공", Toast.LENGTH_SHORT).show()
                }, 0)
            } else {
                println("No file to download. Server replied HTTP code: ${conn.responseCode}")
                handler.postDelayed({
                    Toast.makeText(context, "파일 다운로드 실패 : ${conn.responseCode}", Toast.LENGTH_SHORT)
                        .show()
                }, 0)
            }
            conn.disconnect()
        } catch (e: Exception) {
            println("An error occurred while trying to download a file.")
            e.printStackTrace()
            try {
                `is`?.close()
                os?.flush()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
}

@SuppressLint("SimpleDateFormat")
fun loadFile(userToken: String ,imageView: ImageView, data : DocFileDownLoadDTO) {
    Thread {
        var `is`: InputStream? = null
        val handler = Handler(Looper.getMainLooper())
        try {
            //Header
            val url = URL("${CodeList.portNum}/commManage/docFileDownload")
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



//변수명 받아오는 테스트 함수
@SuppressLint("SimpleDateFormat")
fun test(userToken: String , post_uuid : String, parent_uuid : String) {
    Thread {
        var `is`: InputStream? = null
        try {
            //Query, Path
            val url = URL("${CodeList.portNum}/projMessageBoardManage/MessageBoardReply/{$post_uuid}?parent_uuid=$parent_uuid")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-Type", "application/json;utf-8")
            conn.setRequestProperty("Accept", "application/json")
            //Header
            conn.setRequestProperty("token", userToken)
            conn.setRequestProperty("sysCd", CodeList.sysCd)
            conn.connectTimeout = 1500

            Log.d("response_fail", conn.responseCode.toString())
            Log.d("response_fail", conn.responseMessage.toString())

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = conn.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var inputLine: String?
                while (bufferedReader.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
                val responseData = response.toString()
                Log.d("response_test_form", responseData)
            conn.disconnect()
        }
        }catch (e: Exception) {
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