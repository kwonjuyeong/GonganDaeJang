package com.gonggan.objects

import android.Manifest
import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

//저장소 사용자 권한 요청(안드로이드 정책)
fun requestMultiplePermissions(context: Context) {
    Dexter.withActivity((context as Activity))
        .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.isAnyPermissionPermanentlyDenied) {
                    Toast.makeText(context, "저장소 및 카메라 권한이 거절되었습니다.\n[설정]에서 권한을 허용해주세요.", Toast.LENGTH_SHORT).show() } }
            override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest?>?, token: PermissionToken) { token.continuePermissionRequest() } }).withErrorListener {
            Toast.makeText(context, "저장소 권한체크를 해주세요!", Toast.LENGTH_SHORT).show() }
        .onSameThread()
        .check()
}