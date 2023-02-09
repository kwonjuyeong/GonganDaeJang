package com.example.gonggandaejang

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.allscapeservice.a22allscape_app.DTO.PostGalleryDTO
import com.allscapeservice.a22allscape_app.objects.*
import com.example.gonggandaejang.API.PostGallery
import com.example.gonggandaejang.Adapter.GalleryPostAdapter
import com.example.gonggandaejang.Adapter.PhotoInfo
import com.example.gonggandaejang.Adapter.PostGalleryData
import com.example.gonggandaejang.databinding.ActivityPhotoGalleryPostDocumentBinding
import com.example.gonggandaejang.objects.CodeList
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

//사진대지 등록

private var photo: PostGalleryDTO? = null

class PhotoGalleryPostDocument : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoGalleryPostDocumentBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var constCode : String

    //카메라 uri
    private var photoURI : Uri? = null
    //파일 선택 모듈 선언
    private val usiPathUtil = UriPathUtils()
    //사진 데이터 저장
    private var photoData = ArrayList<PostGalleryData>()

    //사진 리스트 데이터(info)
    private val photoInfoList = ArrayList<PhotoInfo>()

    //사진 File 리스트
    private var photoBoardFileList = ArrayList<File>()
    private var photoBoardFileBody = ArrayList<MultipartBody.Part>()

    //전송 파일 통합
    private var hashFile = ArrayList<MultipartBody.Part>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoGalleryPostDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        //사진
        binding.requestRecycler.layoutManager = LinearLayoutManager(this)
        binding.requestRecycler.adapter = GalleryPostAdapter(photoData) { onDeleteItems(it) }

        //사진대지 추가
        binding.photoListAddBtn.setOnClickListener {
            if(binding.pictureDate.text.toString() == ""){
                Toast.makeText(this, "날짜 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
            }else if(binding.title.text.toString() == ""){
                Toast.makeText(this, "파일 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else{
                myDig()
            }
        }

        binding.visitDatePicker.setOnClickListener {
            binding.pictureDate.text = ""
            val cal = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                binding.pictureDate.text = getString(R.string.calender_day_format, year.toString(), getMonth(month), getDay(dayOfMonth))
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(
                Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = Date().time }.show()
        }

        //문서 등록===================================================================================================================================================
        binding.bottomBtn.setOnClickListener {
            if(hashFile.size <1 &&photoData.size <1){
                Toast.makeText(this, "사진이 첨부되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }else{
                for (i in 0 until photoData.size) {
                    //사진 정보
                    photoInfoList.add(photoData[i].info)

                    //파일 업로드(Multipart 변환)
                    photoBoardFileList.add(photoData[i].file)
                    photoBoardFileBody.add(photoBoardFileList[i].let { it1 -> RequestBody.create(MediaType.parse("multipart/form-data"), it1) }.let { it1 -> MultipartBody.Part.createFormData("f_" + i + "_image", photoBoardFileList[i].name, it1) })

                    hashFile.add(photoBoardFileBody[i])
                }

                //Json 데이터
                val gson = Gson()
                val jsonObject = JSONObject("{\"image_info_list\" : ${gson.toJson(photoInfoList)}}").toString()
                val jsonBody = RequestBody.create(MediaType.parse("application/json"), jsonObject)


                val postDate = convertDateFormat4(binding.pictureDate.text.toString())
                Log.d("input_consCode", constCode)
                Log.d("input_consDate", postDate)
                Log.d("input_json", jsonObject)
                for(i in 0 until hashFile.size){
                    Log.d("input_file", hashFile[i].toString())
                }
                val retrofit = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projPhotoSiteManage/photoSite/{cons_code}/{cons_date}/")
                val postPhoto: PostGallery = retrofit.create(PostGallery::class.java)

                postPhoto.requestPostGALLERY(constCode, postDate, CodeList.sysCd, userToken, jsonBody, hashFile).enqueue(object :
                    Callback<PostGalleryDTO> {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onFailure(call: Call<PostGalleryDTO>, t: Throwable) { Log.d("retrofit", t.toString())
                    hashFile.clear()
                    photoData.clear()
                    binding.requestRecycler.adapter?.notifyDataSetChanged()}
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(call: Call<PostGalleryDTO>, response: Response<PostGalleryDTO>) {
                        photo = response.body()
                        Log.d("codes", photo?.code.toString())
                        Log.d("codes", photo?.msg.toString())
                        Log.d("codes", photo?.value.toString())
                        if (photo?.code == 200) {
                            Toast.makeText(this@PhotoGalleryPostDocument, "등록완료", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@PhotoGalleryPostDocument, "등록 실패 : ${photo?.msg}", Toast.LENGTH_SHORT).show()
                        }
                        finish()
                    }
                })

            }
        }

    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        constCode = intent.getStringExtra("code")!!
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onDeleteItems(data : PostGalleryData){
        photoData.remove(data)
        binding.requestRecycler.adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) { return }
        when (requestCode) {
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

                            } finally {
                                out.close()
                            }
                        } finally {
                            `in`.close()
                        }
                    }
                    val title = binding.title.text.toString()
                    val bitmap = BitmapFactory.decodeFile(file.path)
                    photoData.add(PostGalleryData(file, bitmap, PhotoInfo(filename, title)))
                    binding.requestRecycler.adapter?.notifyDataSetChanged()
                }
                binding.title.setText("")
            }
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
                        val title = binding.title.text.toString()

                        var bitmap = BitmapFactory.decodeFile(file.path)

                        val exif = ExifInterface(file.path)
                        val exifOrientation: Int = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                        val exifDegree = exifOrientationToDegrees(exifOrientation)
                        bitmap = rotate(bitmap, exifDegree)
                        photoData.add(PostGalleryData(file ,bitmap, PhotoInfo(filename, title)))
                        binding.requestRecycler.adapter?.notifyDataSetChanged()
                    }
                    binding.title.setText("")
                }
            }
        }
    }


    //Uri to 파일명 추출 함수
    private fun getName(uri: Uri?): String {
        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val cursor = managedQuery(uri, projection, null, null, null)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }

    //카메라 찍기(V2)
    private fun createImageUri(filename:String): Uri?{
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME,filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        return contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    @SuppressLint("SimpleDateFormat")
    private fun dispatchTakePictureIntentEx()
    {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri : Uri? = createImageUri("JPEG_${timeStamp}")
        photoURI = uri
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, CodeList.Camera)
    }

    private fun myDig(){
        val dialog = Dialog(this@PhotoGalleryPostDocument)

        dialog.setContentView(R.layout.custom_dialog_camera)

        val text =dialog.findViewById<TextView>(R.id.camera_title)
        val camera = dialog.findViewById<Button>(R.id.camera_btn)
        val album = dialog.findViewById<Button>(R.id.album_btn)

        text.text = "사진대지 사진 등록"

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

    fun exifOrientationToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    fun rotate(bitmap: Bitmap?, degrees: Int): Bitmap? { // 이미지 회전 및 이미지 사이즈 압축
        var bitmap = bitmap
        if (degrees != 0 && bitmap != null) {
            val m = Matrix()
            m.setRotate(degrees.toFloat(), bitmap.width.toFloat() / 2,
                bitmap.height.toFloat() / 2)
            try {
                val converted = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.width, bitmap.height, m, true)
                if (bitmap != converted) {
                    bitmap.recycle()
                    bitmap = converted
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 4
                    bitmap = Bitmap.createScaledBitmap(bitmap, 1280, 1280, true) // 이미지 사이즈 줄이기
                }
            } catch (ex: OutOfMemoryError) {
            }
        }
        return bitmap
    }

}