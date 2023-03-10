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
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityQapostBinding
import com.gonggan.API.PostQADoc
import com.gonggan.Adapter.QAPostData
import com.gonggan.Adapter.QaPostDocFileAdapter
import com.gonggan.DTO.PostQADTO
import com.gonggan.DTO.QADocFileData
import com.gonggan.objects.ApiUtilities
import com.gonggan.objects.CodeList
import com.gonggan.objects.UriPathUtils
import com.gonggan.source.detailhome.RootActivity
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


private var postQA : PostQADTO ?= null
private var photoURI : Uri? = null

private const val TAG = "QA_POST_DATA"

class QAPost : AppCompatActivity() {
    private lateinit var binding: ActivityQapostBinding
    private lateinit var sharedPreference : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private lateinit var userToken : String
    private lateinit var consCode : String

    private val usiPathUtil = UriPathUtils()

    private var fileData = ArrayList<QAPostData>()
    private var hashFile = ArrayList<MultipartBody.Part>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQapostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.apply {
            title = getString(R.string.communication_post)
        }

        val retrofitPostQA = ApiUtilities.callRetrofit("http://211.107.220.103:${CodeList.portNum}/projMessageBoardManage/MessageBoard/{cons_code}/").create(PostQADoc::class.java)

        binding.filesRecycler.apply {
            layoutManager = LinearLayoutManager(this@QAPost)
            adapter = QaPostDocFileAdapter(fileData) { qaDelete(it) }
        }

        binding.postFileBtn.setOnClickListener {
            pickCamera()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    binding.bottomBtn.setOnClickListener {

        if(binding.titleText.text.isEmpty()){
            Toast.makeText(this@QAPost, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
        }else if(binding.contentText.text.isEmpty()){
            Toast.makeText(this@QAPost, "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
            }else{

            val title = binding.titleText.text.toString()
            val content = binding.contentText.text.toString()

            //사진 hashFile이랑 정보 넣어주기
            for (i in 0 until fileData.size) {
                hashFile.add(fileData[i].file.let { it1 -> RequestBody.create(MediaType.parse("multipart/form-data"), it1) }.let { it1 -> MultipartBody.Part.createFormData("f_$i", fileData[i].file?.name, it1) })
            }

            //서버에 보낼 Json Object 생성
            val gson = Gson()
            val jsonObject = JSONObject("{\"post_type\":\"0\", \"title\":\"${title}\", \"content\":\"${content}\"}").toString()
            val jsonBody = RequestBody.create(MediaType.parse("application/json"), jsonObject)

            Log.d(TAG, jsonObject)
            Log.d(TAG, hashFile.size.toString())

            retrofitPostQA.requestWatchQa(consCode, CodeList.sysCd, userToken, jsonBody, hashFile).enqueue(object :
            Callback<PostQADTO> {
            override fun onFailure(call: Call<PostQADTO>, t: Throwable) { Log.d("Post_error", t.toString()) }

            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<PostQADTO>, response: Response<PostQADTO>) {
                postQA = response.body()

                Log.d(TAG, postQA?.code.toString())
                Log.d(TAG, postQA?.msg.toString())
                Log.d(TAG, postQA?.value.toString())

                if(postQA?.code == 200){
                    Toast.makeText(this@QAPost, "등록 성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@QAPost, RootActivity::class.java)
                    intent.putExtra("TAG", "QA")
                    intent.putExtra("code", consCode)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this@QAPost, "등록 실패 ${postQA?.msg}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    }
    }

    private fun init(){
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
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
                                fileData.add(QAPostData(filename, file))
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
                            fileData.add(QAPostData(filename, file))
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
                        fileData.add(QAPostData(filename, file))
                        binding.filesRecycler.adapter?.notifyDataSetChanged()
                    }

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
        val dialog = Dialog(this@QAPost)
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
    private fun qaDelete(data : QAPostData){
        fileData.remove(data)
        binding.filesRecycler.adapter?.notifyDataSetChanged()
    }

   @Deprecated("Deprecated in Java")
   override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@QAPost, RootActivity::class.java)
        intent.putExtra("TAG", "QA")
        intent.putExtra("code", consCode)
        startActivity(intent)
        finish()
    }

}