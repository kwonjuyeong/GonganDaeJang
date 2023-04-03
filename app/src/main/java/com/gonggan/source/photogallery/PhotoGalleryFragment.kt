package com.gonggan.source.photogallery

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.gonggan.databinding.FragmentPhotoGalleryBinding
import com.gonggan.DTO.GetGallery
import com.gonggan.objects.*
import com.gonggan.API.GetGalleryPic
import com.gonggan.Adapter.GalleryAdapter
import com.gonggan.Adapter.GalleryData
import com.gonggan.Adapter.GalleryListData
import com.gonggan.objects.ApiUtilities.callRetrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "PhotoGalleryFragment"

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
        super.onCreate(savedInstanceState)

        binding = FragmentPhotoGalleryBinding.inflate(layoutInflater)

        arguments?.let {
            token = it.getString("token").toString()
            consCode = it.getString("code").toString()
        }

        binding.galleryRecycler.apply {
            val snapHelper: SnapHelper = PagerSnapHelper()
            layoutManager = LinearLayoutManager(requireContext())
            adapter = GalleryAdapter(galleryData, token.toString())
            if(onFlingListener == null) snapHelper.attachToRecyclerView(binding.galleryRecycler)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(@NonNull recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }
                override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                }
            })
        }

        beforeWeekDate()
        updatePhotoGallery(1, allStartDate, allEndDate)
        //검색조건 - 날짜 선택=========================================================================
        binding.startDatePicker.setOnClickListener {
            callSelectCalendar(binding.startDate, requireContext())
        }
        binding.endDatePicker.setOnClickListener {
            callSelectCalendar(binding.endDate, requireContext())
        }

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
            updatePhotoGallery(0, startDate, endDate)
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

    private fun beforeWeekDate(){
        val calculator = Calendar.getInstance()
        calculator.time = Date()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        allEndDate = endDate(df.format(calculator.time))
        binding.endDate.text = convertDateFormat(allEndDate)
        calculator.add(Calendar.DATE, -7)
        allStartDate = startDate(df.format(calculator.time))
        binding.startDate.text = convertDateFormat(allStartDate)
    }

    private fun updatePhotoGallery(state : Int, searchStart : String, searchEnd : String){
        val galleryService = callRetrofit("${CodeList.portNum}/projWorkLogManage/searchWorkImgList/{consCode}/{searchStartDate}/{searchEndDate}/").create(GetGalleryPic::class.java)

        galleryService.requestGetGalleryPicture(consCode.toString(), searchStart, searchEnd, token.toString() ,CodeList.sysCd).enqueue(object :
            Callback<GetGallery> {
            override fun onFailure(call: Call<GetGallery>, t: Throwable) {}
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<GetGallery>, response: Response<GetGallery>) {
                gallery = response.body()

                val insideList = ArrayList<String>()
                if(gallery?.value?.size != 0){
                    galleryData.clear()
                    for (i in 0 until gallery?.value?.size!!) {
                        if (!insideList.contains(convertDateFormat(gallery?.value?.get(i)?.upload_date))) {
                            insideList.add(convertDateFormat(gallery?.value?.get(i)?.upload_date))
                            val division = arrayListOf<GalleryListData>()
                            galleryInputData = GalleryData(gallery?.value?.get(i)?.cons_date.toString(), division)
                            galleryData.add(galleryInputData)
                        }
                    }
                    for (m in 0 until galleryData.size) {
                        for (j in 0 until gallery?.value?.size!!) {
                            if (convertDateFormat(galleryData[m].cons_date) == convertDateFormat(gallery?.value?.get(j)?.cons_date)) {
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
                    if(state ==0){
                        binding.startDate.text = ""
                        binding.endDate.text = ""
                    }
                }
            }
        })
    }

}