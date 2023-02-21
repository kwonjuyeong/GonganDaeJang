package com.example.gonggandaejang.objects

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.allscapeservice.a22allscape_app.objects.convertDateFormat
import com.example.gonggandaejang.R
import kotlin.math.sqrt

internal enum class TOUCHMODE {
    NONE,
    SINGLE,
    MULTI
}
private var touchMode: TOUCHMODE? = null
private var matrix : Matrix? = null
private var savedMatrix : Matrix? = null
private var startPoint: PointF? = null
private var midPoint : PointF? = null
private var oldDistance = 0f

@SuppressLint("StaticFieldLeak")
private lateinit var images : ImageView

@SuppressLint("ClickableViewAccessibility")
fun customDetailGallery(context: Context, token: String, consCode : String, path : String, originName : String, changeName : String, uploadDate : String, title : String){
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.custom_dialog_galley_detail_watch)

    val titles =dialog.findViewById<TextView>(R.id.textView14)
    images = dialog.findViewById(R.id.detail_gallery_imageview)
    val ok = dialog.findViewById<Button>(R.id.ok_btn)
    val filename = dialog.findViewById<TextView>(R.id.detail_gallery_file_name)
    val date = dialog.findViewById<TextView>(R.id.detail_gallery_date)

    titles.text = "사진 정보"
    filename.text = title
    date.text = convertDateFormat(uploadDate)

    loadFile(token, images, DocFileDownLoadDTO(consCode, "", path, originName, changeName))

    matrix = Matrix()
    savedMatrix = Matrix()

    images.setOnTouchListener(onTouch)
    images.scaleType = ImageView.ScaleType.MATRIX

    ok.setOnClickListener {
        dialog.dismiss()
    }
    dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    dialog.setCanceledOnTouchOutside(true)
    dialog.setCancelable(true)
    dialog.show()
}

@SuppressLint("ClickableViewAccessibility")
private val onTouch = View.OnTouchListener { v, event ->
    if (v == images) {
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                touchMode = TOUCHMODE.SINGLE
                downSingleEvent(event)
            }
            MotionEvent.ACTION_POINTER_DOWN -> if (event.pointerCount == 2) { // 두손가락 터치를 했을 때
                touchMode = TOUCHMODE.MULTI
                downMultiEvent(event)
            }
            MotionEvent.ACTION_MOVE -> if (touchMode === TOUCHMODE.SINGLE) {
                moveSingleEvent(event)
            } else if (touchMode === TOUCHMODE.MULTI) {
                moveMultiEvent(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> touchMode = TOUCHMODE.NONE
        }
    }
    true
}

//두 손가락의 중간점
private fun getMidPoint(e: MotionEvent): PointF {
    val x = (e.getX(0) + e.getX(1)) / 2
    val y = (e.getY(0) + e.getY(1)) / 2
    return PointF(x, y)
}

//두 손가락 간 거리
private fun getDistance(e: MotionEvent): Float {
    val x = e.getX(0) - e.getX(1)
    val y = e.getY(0) - e.getY(1)
    return sqrt((x * x + y * y).toDouble()).toFloat()
}

//한 손가락 시작점 구하는 함수
private fun downSingleEvent(event: MotionEvent) {
    savedMatrix!!.set(matrix)
    startPoint = PointF(event.x, event.y)
}

//한 손가락 이동
private fun moveSingleEvent(event: MotionEvent) {
    matrix!!.set(savedMatrix)
    matrix!!.postTranslate(event.x - startPoint!!.x, event.y - startPoint!!.y)
    images.imageMatrix = matrix
}

//두 손가락 시작점 구하는 함수
private fun downMultiEvent(event: MotionEvent) {
    oldDistance = getDistance(event)
    if (oldDistance > 5f) {
        savedMatrix!!.set(matrix)
        midPoint = getMidPoint(event)
    }
}

//두 손가락 이동
private fun moveMultiEvent(event: MotionEvent) {
    val newDistance = getDistance(event)
    if (newDistance > 5f) {
        matrix!!.set(savedMatrix)
        val scale = newDistance / oldDistance
        matrix!!.postScale(scale, scale, midPoint!!.x, midPoint!!.y)
        matrix!!.postRotate(0.0.toFloat(), midPoint!!.x, midPoint!!.y)
        images.imageMatrix = matrix
    }
}
