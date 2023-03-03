package com.gonggan.source.dailywork

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
import com.example.gonggan.databinding.ActivityWorkEditPhotoBinding
import com.gonggan.API.DeleteGallery
import com.gonggan.API.PostGallery
import com.gonggan.Adapter.ConsWorkInfoInsideFileAdapter
import com.gonggan.Adapter.ConsWorkInputList
import com.gonggan.Adapter.ImageInputList
import com.gonggan.DTO.PostGalleryDTO
import com.gonggan.objects.CodeList
import com.gonggan.objects.UriPathUtils
import com.gonggan.objects.callRetrofit
import com.gonggan.objects.loadFile
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


private var ImageInfoData = arrayListOf<ImageInputList>()
private lateinit var ImageInfoInputData : ImageInputList

private var photoURI : Uri? = null
private val usiPathUtil = UriPathUtils()

private var delete : PostGalleryDTO?= null

class WorkEditPhoto : AppCompatActivity() {
    private lateinit var binding: ActivityWorkEditPhotoBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var consCode : String
    private lateinit var sysDocNum : String
    private lateinit var workLogConsCode : String
    private lateinit var consDate : String
    private lateinit var consTypeCd : String
    private lateinit var imageGetData : ConsWorkInputList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWorkEditPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        //이미지 조회================================================================================================
        binding.imageRecycler.layoutManager = LinearLayoutManager(this)
        binding.imageRecycler.adapter = ConsWorkInfoInsideFileAdapter(ImageInfoData, {deleteBtn(it)}, userToken, consCode, sysDocNum)

        ImageInfoData.clear()
        for(i in 0 until imageGetData.imageList.size){
            ImageInfoInputData = ImageInputList(imageGetData.imageList[i].change_name, imageGetData.imageList[i].cons_date, imageGetData.imageList[i].cons_type_cd, imageGetData.imageList[i].cons_type_nm, imageGetData.imageList[i].cons_type_explain, imageGetData.imageList[i].file_index, imageGetData.imageList[i].origin_name, imageGetData.imageList[i].file_path, imageGetData.imageList[i].title, imageGetData.imageList[i].upload_date)
            ImageInfoData.add(ImageInfoInputData)
        }
        //=========================================================================================================

        binding.addImageBtn.setOnClickListener {
            if(binding.title.text.toString() == ""){
             Toast.makeText(this@WorkEditPhoto, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
            }else{
                pickCamera()
            }
        }


        binding.bottomBtn.setOnClickListener {
            val intent = Intent(this, DailyWorkDocument::class.java)
            intent.putExtra("sysDocNum", sysDocNum)
            intent.putExtra("code", consCode)
            startActivity(intent)
            finish()
        }
    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        consCode = intent.getStringExtra("code")!!
        sysDocNum = intent.getStringExtra("sysDocCode")!!
        workLogConsCode = intent.getStringExtra("work_log_cons_code")!!
        consDate = intent.getStringExtra("cons_date")!!
        consTypeCd = intent.getStringExtra("cons_type_cd")!!
        imageGetData = intent.getSerializableExtra("data") as ConsWorkInputList
    }

    //사진 등록
    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            //사진대지 - 파일 탐색기에서 가져오기
            CodeList.Album -> {
                data?:return
                val uri = data.data as Uri
                val filename = getName(uri)
                val imgPath =  usiPathUtil.getRealPathFromURI(this, uri)

                if (filename != "" && imgPath != null) {
                    val `in` = contentResolver.openInputStream(uri) //src
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

                                val title = binding.title.text.toString()

                                val retroPhoto = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/WorkDLImage/{cons_code}/{sys_doc_num}/{file_index}/")
                                val photoPost: PostGallery = retroPhoto.create(PostGallery::class.java)

                                val fileBody = file.let { it1 -> RequestBody.create(MediaType.parse("multipart/form-data"), it1) }.let { it1 -> MultipartBody.Part.createFormData("f_image" , file.name, it1) }

                                val jsonObject = JSONObject("{\"cons_date\":\"${consDate}\", \"work_log_cons_code\":\"${workLogConsCode}\",\"cons_type_cd\":\"${consTypeCd}\", \"title\":\"${title}\"}").toString()
                                val jsonBody = RequestBody.create(MediaType.parse("application/json"), jsonObject)
                                Log.d("json", jsonObject.toString())

                                photoPost.requestPostGallery(consCode, sysDocNum, ImageInfoData.size + 1 ,
                                    CodeList.sysCd, userToken, jsonBody, fileBody).enqueue(object :
                                    Callback<PostGalleryDTO> {
                                    override fun onFailure(call: Call<PostGalleryDTO>, t: Throwable) {Log.d("retro", t.toString())}
                                    @SuppressLint("NotifyDataSetChanged")
                                    override fun onResponse(call: Call<PostGalleryDTO>, response: Response<PostGalleryDTO>) {
                                        delete = response.body()

                                        if(delete?.code == 200){
                                            Toast.makeText(this@WorkEditPhoto, "사진 등록", Toast.LENGTH_SHORT).show()
                                            ImageInfoInputData = ImageInputList("","1","","", "", ImageInfoData.size , filename ,file.path , title,"")
                                            ImageInfoData.add(ImageInfoInputData)
                                            binding.imageRecycler.adapter?.notifyDataSetChanged()
                                            binding.title.setText("")
                                        }else{
                                            Toast.makeText(this@WorkEditPhoto, "사진 등록 실패", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })

                            } finally {
                                out.close()
                            }
                        } finally {
                            `in`.close()
                        }
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

                                    val title = binding.title.text.toString()

                                    val retroPhoto = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/WorkDLImage/{cons_code}/{sys_doc_num}/{file_index}/")
                                    val photoPost: PostGallery = retroPhoto.create(PostGallery::class.java)

                                    val fileBody = file.let { it1 -> RequestBody.create(MediaType.parse("multipart/form-data"), it1) }.let { it1 -> MultipartBody.Part.createFormData("f_image" , file.name, it1) }

                                    val jsonObject = JSONObject("{\"cons_date\":\"${consDate}\", \"work_log_cons_code\":\"${workLogConsCode}\",\"cons_type_cd\":\"${consTypeCd}\", \"title\":\"${title}\"}").toString()
                                    val jsonBody = RequestBody.create(MediaType.parse("application/json"), jsonObject)

                                    Log.d("json", jsonObject)

                                    photoPost.requestPostGallery(consCode, sysDocNum, ImageInfoData.size ,CodeList.sysCd, userToken, jsonBody, fileBody).enqueue(object :
                                        Callback<PostGalleryDTO> {
                                        override fun onFailure(call: Call<PostGalleryDTO>, t: Throwable) {Log.d("retro", t.toString())}
                                        @SuppressLint("NotifyDataSetChanged")
                                        override fun onResponse(call: Call<PostGalleryDTO>, response: Response<PostGalleryDTO>) {
                                            delete = response.body()

                                            if(delete?.code == 200){
                                                Toast.makeText(this@WorkEditPhoto, "사진 등록", Toast.LENGTH_SHORT).show()
                                                ImageInfoInputData = ImageInputList("",consDate ,consTypeCd,"", "", ImageInfoData.size , filename ,"" , title,"")
                                                ImageInfoData.add(ImageInfoInputData)
                                                binding.imageRecycler.adapter?.notifyDataSetChanged()
                                                binding.title.setText("")
                                            }else{
                                                Toast.makeText(this@WorkEditPhoto, "사진 등록 실패", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    })
                                } finally {
                                    out.close()
                                }
                            } finally {
                                `in`.close()
                            }
                        }
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

    //Uri to 파일명 추출 함수
    private fun getName(uri: Uri?): String {
        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val cursor = managedQuery(uri, projection, null, null, null)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
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

    //사진 등록
    private fun pickCamera(){
        val dialog = Dialog(this@WorkEditPhoto)
        dialog.setContentView(R.layout.custom_dialog_camera)

        val text =dialog.findViewById<TextView>(R.id.camera_title)
        val camera = dialog.findViewById<Button>(R.id.camera_btn)
        val album = dialog.findViewById<Button>(R.id.album_btn)

        text.text = "사진대지 등록"

        camera.setOnClickListener {
            dispatchTakePictureIntentEx()
            dialog.dismiss()
        }
        album.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, CodeList.Album)
            dialog.dismiss()
        }
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
    }


    //갤러리 사진 삭제
    @SuppressLint("NotifyDataSetChanged")
    private fun deleteBtn(data : ImageInputList){
        val retroDeletePhoto = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/WorkDLImage/{cons_code}/{sys_doc_num}/{work_log_cons_code}/{file_index}/")
        val photoDelete: DeleteGallery = retroDeletePhoto.create(DeleteGallery::class.java)

        Log.d("input_consCode", consCode)
        Log.d("input_sysDocCode", sysDocNum)
        Log.d("input_fileIndex", data.file_index.toString())
        Log.d("input_sysCd", CodeList.sysCd)
        Log.d("input_token", userToken)

        photoDelete.requestDeleteGallery(consCode, sysDocNum, workLogConsCode, data.file_index, CodeList.sysCd, userToken).enqueue(object :
            Callback<PostGalleryDTO> {
            override fun onFailure(call: Call<PostGalleryDTO>, t: Throwable) {Log.d("retro", t.toString())}
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<PostGalleryDTO>, response: Response<PostGalleryDTO>) {
                delete = response.body()
                Log.d("delete_gallery_code", delete?.code.toString())
                Log.d("delete_gallery_code", delete?.msg.toString())
                Log.d("delete_gallery_code", delete?.value.toString())
                if(delete?.code == 200){
                    Toast.makeText(this@WorkEditPhoto, "삭제 완료", Toast.LENGTH_SHORT).show()
                    ImageInfoData.remove(data)
                    binding.imageRecycler.adapter?.notifyDataSetChanged()
                }

            }
        })
    }



}