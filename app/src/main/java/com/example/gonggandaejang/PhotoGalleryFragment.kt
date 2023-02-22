package com.example.gonggandaejang

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.allscapeservice.a22allscape_app.DTO.GetGallery
import com.allscapeservice.a22allscape_app.objects.*
import com.example.gonggandaejang.API.GetGalleryPic
import com.example.gonggandaejang.Adapter.GalleryAdapter
import com.example.gonggandaejang.Adapter.GalleryData
import com.example.gonggandaejang.Adapter.GalleryListData
import com.example.gonggandaejang.databinding.FragmentPhotoGalleryBinding
import com.example.gonggandaejang.objects.CodeList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private var token : String? = null
private var consCode : String? = null
private var gallery : GetGallery?= null
class PhotoGalleryFragment : Fragment() {
    private lateinit var binding: FragmentPhotoGalleryBinding
    private lateinit var startDate : String
    private lateinit var endDate : String
    private lateinit var allStartDate : String
    private lateinit var allEndDate : String

    private var galleryData = arrayListOf<GalleryData>()
    private lateinit var galleryInputData : GalleryData

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = FragmentPhotoGalleryBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString("token").toString()
            consCode = it.getString("code").toString()
        }

        binding.galleryRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.galleryRecycler.adapter = GalleryAdapter(context, galleryData, token.toString())
        val snapHelper: SnapHelper = PagerSnapHelper()
        if (  binding.galleryRecycler.onFlingListener == null) snapHelper.attachToRecyclerView(  binding.galleryRecycler)

        binding.galleryRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(@NonNull recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        val calculator = Calendar.getInstance()
        calculator.time = Date()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")

        allEndDate =endDate(df.format(calculator.time))
        calculator.add(Calendar.DATE, -7)
        allStartDate = startDate(df.format(calculator.time))

        val retrofitGallery = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/searchWorkImgList/{consCode}/{searchStartDate}/{searchEndDate}/")
        val galleryService: GetGalleryPic = retrofitGallery.create(GetGalleryPic::class.java)

        galleryService.requestGetGalleryPicture(consCode.toString(), allStartDate, allEndDate, token.toString() ,CodeList.sysCd).enqueue(object :
            Callback<GetGallery> {
            override fun onFailure(call: Call<GetGallery>, t: Throwable) {}
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<GetGallery>, response: Response<GetGallery>) {
                gallery = response.body()
                galleryData.clear()
                Log.d("gallery", gallery?.value.toString())
                val insideList = ArrayList<String>()
                if(gallery?.value?.size != 0){
                for (i in 0 until gallery?.value?.size!!) {
                    Log.d("input_data_all", convertDateFormat(gallery?.value?.get(i)?.upload_date))
                    if (!insideList.contains(convertDateFormat(gallery?.value?.get(i)?.upload_date))) {
                        insideList.add(convertDateFormat(gallery?.value?.get(i)?.upload_date))
                        val division = arrayListOf<GalleryListData>()
                        galleryInputData = GalleryData(gallery?.value?.get(i)?.upload_date.toString(), division)
                        galleryData.add(galleryInputData)
                    }
                }

                for (m in 0 until galleryData.size) {
                    for (j in 0 until gallery?.value?.size!!) {
                        if (convertDateFormat(galleryData[m].upload_date) == convertDateFormat(gallery?.value?.get(j)?.upload_date)) {
                            if (gallery?.value?.get(j)?.cons_code != null) {
                                galleryData[m].GalleryList.add(GalleryListData(gallery?.value?.get(j)?.co_code.toString(),gallery?.value?.get(j)?.cons_code.toString(),gallery?.value?.get(j)?.cons_date.toString(),
                                    gallery?.value?.get(j)?.file_index!!.toInt(),gallery?.value?.get(j)?.image_chan_name.toString(),gallery?.value?.get(j)?.image_orig_name.toString(),gallery?.value?.get(j)?.image_path.toString()
                                    ,gallery?.value?.get(j)?.image_title.toString(), gallery?.value?.get(j)?.item_code.toString(),gallery?.value?.get(j)?.pc_code.toString(),gallery?.value?.get(j)?.pc_name.toString(),
                                    gallery?.value?.get(j)?.product.toString(),gallery?.value?.get(j)?.standard.toString(), gallery?.value?.get(j)?.upload_date.toString()))
                            }
                        }
                    }
                }
                binding.galleryRecycler.adapter?.notifyDataSetChanged()
            }
            }
        })
        //검색조건 - 날짜 선택=============================================================================================================================================
        binding.startDatePicker.setOnClickListener {
            startDate = ""
            binding.startDate.text = ""
            val cal = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                binding.startDate.text = getString(R.string.calender_day_format, year.toString(), getMonth(month), getDay(dayOfMonth))
            }
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = Date().time }.show()
        }

        binding.endDatePicker.setOnClickListener {
            startDate = ""
            binding.endDate.text = ""
            val cal = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                binding.endDate.text = getString(R.string.calender_day_format, year.toString(), getMonth(month), getDay(dayOfMonth))
            }
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR),cal.get(
                Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = Date().time }.show()
        }
        //검색조건 - 날짜 선택=============================================================================================================================================
        binding.searchBtn.setOnClickListener {
            endDate = if(binding.endDate.text != ""){
                endDate(binding.endDate.text.toString())
            }else{
                allEndDate
            }
            startDate = if(binding.startDate.text != ""){
                startDate(binding.startDate.text.toString())
            }else{
                allStartDate
            }

            galleryService.requestGetGalleryPicture(consCode.toString(), startDate,endDate, token.toString(), CodeList.sysCd).enqueue(object :
                Callback<GetGallery> { override fun onFailure(call: Call<GetGallery>, t: Throwable) {}
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<GetGallery>, response: Response<GetGallery>) {
                    gallery = response.body()

                    val insideVersionList = ArrayList<String>()

                    galleryData.clear()
                    for (i in 0 until gallery?.value?.size!!) {
                        if (!insideVersionList.contains(convertDateFormat(gallery?.value?.get(i)?.upload_date))) {
                            insideVersionList.add(convertDateFormat(gallery?.value?.get(i)?.upload_date.toString()))
                            val division = arrayListOf<GalleryListData>()

                            galleryInputData = GalleryData(gallery?.value?.get(i)?.upload_date.toString(), division)
                            galleryData.add(galleryInputData)

                            for (m in 0 until galleryData.size) {
                                for (j in 0 until gallery?.value?.size!!) {
                                    if (convertDateFormat(galleryData[m].upload_date) == convertDateFormat(gallery?.value?.get(j)?.upload_date)) {
                                        if (gallery?.value?.get(j)?.cons_code != null) {
                                            galleryData[m].GalleryList.add(GalleryListData(gallery?.value?.get(j)?.co_code.toString(),gallery?.value?.get(j)?.cons_code.toString(),gallery?.value?.get(j)?.cons_date.toString(),
                                                gallery?.value?.get(j)?.file_index!!.toInt(),gallery?.value?.get(j)?.image_chan_name.toString(),gallery?.value?.get(j)?.image_orig_name.toString(),gallery?.value?.get(j)?.image_path.toString()
                                                ,gallery?.value?.get(j)?.image_title.toString(), gallery?.value?.get(j)?.item_code.toString(),gallery?.value?.get(j)?.pc_code.toString(),gallery?.value?.get(j)?.pc_name.toString(),
                                                gallery?.value?.get(j)?.product.toString(),gallery?.value?.get(j)?.standard.toString(), gallery?.value?.get(j)?.upload_date.toString()))          }
                                    }
                                }
                            }
                            binding.galleryRecycler.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            })
            binding.startDate.text = ""
            binding.endDate.text = ""
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            PhotoGalleryFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }




}