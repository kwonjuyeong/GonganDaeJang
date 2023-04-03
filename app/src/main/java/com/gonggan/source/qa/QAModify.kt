package com.gonggan.source.qa

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityQamodifyBinding
import com.gonggan.API.ModifyQADoc
import com.gonggan.API.WatchQADoc
import com.gonggan.Adapter.QAModifyAdapter
import com.gonggan.Adapter.QAModifyData
import com.gonggan.DTO.PostQADTO
import com.gonggan.DTO.WatchQADocDTO
import com.gonggan.objects.ApiUtilities
import com.gonggan.objects.CodeList
import com.gonggan.objects.UriPathUtils
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


private const val TAG = "QAModify"

private var modifyQA : PostQADTO?= null
private var photoURI : Uri? = null
private var watchDoc : WatchQADocDTO?= null
private val usiPathUtil = UriPathUtils()
private val indexes = ArrayList<Int>()

class QAModify : AppCompatActivity() {
    private lateinit var binding: ActivityQamodifyBinding
    private lateinit var sharedPreference : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private lateinit var userToken : String
    private lateinit var consCode : String
    private lateinit var uuid : String

    private var fileOrigin = ArrayList<QAModifyData>()
    private var fileListData = ArrayList<QAModifyData>()
    private lateinit var fileInputData: QAModifyData

    //전송할 파일
    private var hashFile = ArrayList<MultipartBody.Part>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQamodifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.apply {
            title = getString(R.string.communication)
        }

        binding.filesRecycler.apply {
            layoutManager = LinearLayoutManager(this@QAModify)
            adapter = QAModifyAdapter(fileListData) { qaDelete(it) }
        }


        //정보 조회========================================================================================================================================================================================
        val retroWatchQaList = ApiUtilities.callRetrofit("${CodeList.portNum}/projMessageBoardManage/MessageBoard/{cons_code}/").create(WatchQADoc::class.java)
        val retroModifyQA = ApiUtilities.callRetrofit("${CodeList.portNum}/projMessageBoardManage/MessageBoard/{cons_code}/").create(ModifyQADoc::class.java)

        retroWatchQaList.requestWatchQa(consCode,CodeList.sysCd, userToken ,uuid).enqueue(object :
            Callback<WatchQADocDTO> {
            override fun onFailure(call: Call<WatchQADocDTO>, t: Throwable) { Log.d("QAWatchDoc_error", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<WatchQADocDTO>, response: Response<WatchQADocDTO>) {
                watchDoc = response.body()
                Log.d(TAG, Gson().toJson(watchDoc?.value))

                binding.titleText.setText(watchDoc?.value?.title.toString())
                val content = Html.fromHtml(watchDoc?.value?.content.toString()).toString()
                binding.contentText.setText(content)

                if(watchDoc?.value?.files?.size != null){
                    for(i in 0 until watchDoc?.value?.files!!.size){
                        fileInputData = QAModifyData(watchDoc?.value?.files?.get(i)?.orig_name.toString(), watchDoc?.value?.files?.get(i)?.file_index!!.toInt(), null)
                        fileOrigin.add(fileInputData)
                        fileListData.add(fileInputData)
                    }
                    Log.d(TAG+"OriginFInput", Gson().toJson(fileOrigin))
                    Log.d(TAG+"ChangeFInput", Gson().toJson(fileListData))
                }
                binding.filesRecycler.adapter?.notifyDataSetChanged()
            }
        })

        //파일 추가 버튼
        binding.postFileBtn.setOnClickListener {
            pickCamera()
        }

        //수정 버튼
        binding.bottomBtn.setOnClickListener {
                indexes.clear()
                for(i in 0 until fileOrigin.size){
                    var hFile = false
                    for(j in 0 until fileListData.size){
                        if(fileOrigin[i].file_name == fileListData[j].file_name){
                            hFile = true
                        }
                    }
                    Log.d(TAG +"hash is", "$hFile")
                    if(!hFile){
                        indexes.add(fileOrigin[i].file_index)
                        Log.d(TAG +"index is", "${fileOrigin[i].file_index}")
                    }
                }

                for(i in 0 until fileListData.size){
                    var hFile = false
                    for(j in 0 until fileOrigin.size){
                        if(fileListData[i].file_name == fileOrigin[j].file_name){
                            hFile = true
                        }
                    }
                    if(!hFile){
                        hashFile.add(fileListData[i].file.let { it1 -> RequestBody.create(MediaType.parse("multipart/form-data"), it1) }.let { it1 -> MultipartBody.Part.createFormData("f_$i", fileListData[i].file?.name, it1) })
                    }
                }

            val title = binding.titleText.text.toString()
            val content = binding.contentText.text.toString()
            //서버에 보낼 Json Object 생성
            val jsonObject = JSONObject("{\"post_type\":\"0\", \"title\":\"${title}\", \"content\":\"${content}\", \"uuid\":\"${uuid}\", \"indexes\":${Gson().toJson(indexes)}}").toString()
            val jsonBody = RequestBody.create(MediaType.parse("application/json"), jsonObject)

            Log.d(TAG, jsonObject)
            Log.d(TAG, consCode)
            Log.d(TAG, CodeList.sysCd)
            Log.d(TAG, userToken)
            Log.d(TAG, hashFile.size.toString())

            retroModifyQA.requestModifyQa(consCode, CodeList.sysCd, userToken, jsonBody, hashFile).enqueue(object :
                Callback<PostQADTO> {
                override fun onFailure(call: Call<PostQADTO>, t: Throwable) { Log.d("Post_error", t.toString()) }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<PostQADTO>, response: Response<PostQADTO>) {
                    modifyQA = response.body()

                    Log.d(TAG, modifyQA?.code.toString())
                    Log.d(TAG, modifyQA?.msg.toString())
                    Log.d(TAG, modifyQA?.value.toString())

                    if(modifyQA?.code == 200){
                        Toast.makeText(this@QAModify, "수정 성공", Toast.LENGTH_SHORT).show()
                        finish()
                    }else{
                        Toast.makeText(this@QAModify, "수정 실패 ${modifyQA?.msg}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        //목록으로 버튼
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
    private fun init(){
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        uuid = intent.getStringExtra("uuid")!!
        consCode = intent.getStringExtra("code")!!
    }
    //사진 등록 함수
    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            CodeList.Album -> {
                data?.let {
                    val clipData = it.clipData
                    if (clipData != null) {
                        // Multiple images selected
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            val filename = getName(uri)
                            val imgPath = usiPathUtil.getRealPathFromURI(this, uri)

                            if (filename != "" && imgPath != null) {
                                val `in` = contentResolver.openInputStream(uri) // src
                                val file = File(applicationContext.filesDir, filename)
                                if (`in` != null) {
                                    try {
                                        val out: OutputStream = FileOutputStream(file) // dst
                                        try {
                                            // Transfer bytes from in to out
                                            val buf = ByteArray(4096)
                                            var len: Int
                                            while (`in`.read(buf).also { len = it } > 0) {
                                                out.write(buf, 0, len)
                                            }
                                        } finally {
                                            out.close()
                                        }
                                    } finally {
                                        `in`.close()
                                    }
                                }
                                fileListData.add(QAModifyData(filename ,fileListData.size+1, file))
                                Log.d(TAG+"data", Gson().toJson(fileListData))
                            }
                        }
                        binding.filesRecycler.adapter?.notifyDataSetChanged()
                    } else {
                        // Single image selected
                        val uri = it.data as Uri
                        val filename = getName(uri)
                        val imgPath = usiPathUtil.getRealPathFromURI(this, uri)

                        if (filename != "" && imgPath != null) {
                            val `in` = contentResolver.openInputStream(uri) // src
                            val file = File(applicationContext.filesDir, filename)
                            if (`in` != null) {
                                try {
                                    val out: OutputStream = FileOutputStream(file) // dst
                                    try {
                                        // Transfer bytes from in to out
                                        val buf = ByteArray(4096)
                                        var len: Int
                                        while (`in`.read(buf).also { len = it } > 0) {
                                            out.write(buf, 0, len)
                                        }

                                    } finally {
                                        out.close()
                                    }
                                } finally {
                                    `in`.close()
                                }
                            }
                            fileListData.add(QAModifyData(filename ,fileListData.size+1, file))
                            Log.d(TAG+"data", Gson().toJson(fileListData))

                        }
                        binding.filesRecycler.adapter?.notifyDataSetChanged()
                    }
                }
            }
            //사진대지 - 카메라에서 가져오기
            CodeList.Camera -> {
                if( photoURI != null)
                {
                    val filename = getName(photoURI)
                    val imgPath =  usiPathUtil.getRealPathFromURI(this, photoURI as Uri)

                    if (filename != "" && imgPath != null) {
                        val `in` = contentResolver.openInputStream(photoURI as Uri) //src
                        val file = File(applicationContext.filesDir, filename)
                        if (`in` != null) {
                            try {
                                val out: OutputStream = FileOutputStream(file) //dst
                                try {
                                    // Transfer bytes from in to out
                                    val buf = ByteArray(4096)
                                    var len: Int
                                    while (`in`.read(buf).also { len = it } > 0) {
                                        out.write(buf, 0, len)
                                    }

                                } finally {
                                    out.close()
                                }
                            } finally {
                                `in`.close()
                            }
                        }
                        fileListData.add(QAModifyData(filename ,fileListData.size+1, file))
                        Log.d(TAG+"data", Gson().toJson(fileListData))
                    }
                    binding.filesRecycler.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    //카메라 찍기(V2)
    private fun createImageUri(filename:String): Uri?{
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME,filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
    //카메라 파일 생성 함수
    @SuppressLint("SimpleDateFormat")
    private fun dispatchTakePictureIntentEx()
    {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri : Uri? =   createImageUri("JPEG_${timeStamp}")
        photoURI = uri
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, CodeList.Camera)
    }

    //Custom Dialog(사진대지 카메라-앨범 선택)
    private fun pickCamera(){
        val dialog = Dialog(this@QAModify)
        dialog.setContentView(R.layout.custom_dialog_camera)

        val text =dialog.findViewById<TextView>(R.id.camera_title)
        val camera = dialog.findViewById<Button>(R.id.camera_btn)
        val album = dialog.findViewById<Button>(R.id.album_btn)

        text.text = "사진 첨부"

        camera.setOnClickListener {
            dispatchTakePictureIntentEx()
            dialog.dismiss()
        }
        album.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            startActivityForResult(intent, CodeList.Album)
            dialog.dismiss()
        }
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
    }

    //Uri to 파일명 추출 함수
    private fun getName(uri: Uri?): String {
        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val cursor = managedQuery(uri, projection, null, null, null)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }

    //사진 삭제 함수
    @SuppressLint("NotifyDataSetChanged")
    private fun qaDelete(data : QAModifyData){
        fileListData.remove(data)
        Log.d(TAG+"data", Gson().toJson(fileListData))
        binding.filesRecycler.adapter?.notifyDataSetChanged()
    }

}