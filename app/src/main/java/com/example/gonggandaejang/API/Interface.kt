package com.example.gonggandaejang.API
//정보 제공 인터페이스
import com.allscapeservice.a22allscape_app.DTO.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

//사용자 로그인
interface LoginService{
    @Headers("Content-Type: application/json")
    @POST("/userManage/login")
    fun requestLogIn(
        @Header("sysCd") sysCd: String,
        @Body LoginRequestDTO: LoginRequestDTO
    ): Call<LoginDTO>
}

//사용자 로그아웃
interface LogoutService {
    @Headers("Content-Type: application/json")
    @GET("/userManage/logout")
    fun requestLogOut(
        @Header("token") token: String,
        @Header("sysCd") sysCd: String
    ): Call<LogoutDTO>
}

//사진대지 조회
interface GetGalleryPic{
    @Headers("Content-Type: application/json")
    @GET("/projWorkLogManage/searchWorkImgList/{consCode}/{searchStartDate}/{searchEndDate}")
    fun requestGetGalleryPicture(
        @Path("consCode") consCode: String,
        @Path("searchStartDate") searchStartDate: String,
        @Path("searchEndDate") searchEndDate: String,
        @Header("token") token: String,
        @Header("sysCd") sysCd: String
    ): Call<GetGallery>
}

//3.1.3 날짜 및 시간 제공
interface GetCurTimeInfoService {
    @Headers("Content-Type: application/json")
    @GET("/commManage/getCurTimeInfo")
    fun requestCurTime(
        @Header("sysCd") sysCd: String?,
        @Header("token") token: String?
    ): Call<GetCurTimeInfoDTO>
}



//코드 리스트
interface GetCodeListService {
    @Headers("Content-Type: application/json")
    @GET("/commManage/getCodeList/{reqType}")
    fun requestGetCodeList(
        @Path("reqType") reqType : String,
        @Header("sysCd") sysCd: String
    ): Call<GetCodeListDTO>
}

//3.2.3 내 정보 요청
interface GetUserInfoService {
    @Headers("Content-Type: application/json")
    @GET("/userManage/getMyInfo")
    fun requestUserInfo(
        @Header("token") token: String?,
        @Header("sysCd") sysCd: String?
    ): Call<UserInfoDTO>
}

//3.2.9 사용자 상세 정보 조회 요청
interface GetUserDetailInfoService {
    @Headers("Content-Type: application/json")
    @GET("/userManage/getUserInfo/{userId}")
    fun requestDetailUserInfo(
        @Path("userId") userId : String,
        @Header("token") token: String?,
        @Header("sysCd") sysCd: String?
    ): Call<UserInfoDTO>
}

//프로젝트 리스트 조회(Project Go)
interface ProjectGoService {
    @Headers("Content-Type: application/json")
    @GET("/projManage/getProjectList/{projectStatus}")
    fun requestProjectsGo(
        @Path("projectStatus") projectStatus : String,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<ProjectGoDTO>
}

//프로젝트 상태 통계현황 조회
interface ProjectListService {
    @Headers("Content-Type: application/json")
    @GET("/projStatistManage/getProjStatusStatistics")
    fun requestProjectsList(
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<ProjectListDTO>
}

//작업일지 조회
interface WorkDiary {
    @Headers("Content-Type: application/json")
    @GET("/projWorkLogManage/WorkDiary/{cons_code}")
    fun requestWorkDiary(
        @Path("cons_code") cons_code : String,
        @Query("start_date") start_date :String,
        @Query("end_date")  end_date : String,
        @Query("start_num") start_num :String,
        @Query("end_num")  end_num : String,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<WorkListResponseDTO>
}

//작업일지 상세 조회
interface WorkDetailDiary{
    @Headers("Content-Type: application/json")
    @GET("/projWorkLogManage/DailyWork/{cons_code}/{sys_doc_num}")
    fun requestDailyWork(
        @Path("cons_code") cons_code : String,
        @Path("sys_doc_num") sys_doc_num : String,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<DailyWorkDTO>
}

//이미지 등록
interface PostGallery{
    @Multipart
    @POST("/projWorkLogManage/WorkDLImage/{cons_code}/{sys_doc_num}/{file_index}")
    fun requestPostGallery(
        @Path("cons_code") cons_code : String,
        @Path("sys_doc_num") sys_doc_num: String,
        @Path("file_index") file_index : String,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String,
        //cons_date, work_log_cons_code, cons_type_cd, title
        @Part("data") data : RequestBody,
        @Part f_image : MultipartBody.Part
    ): Call<PostGalleryDTO>
}

//이미지 삭제
interface DeleteGallery{
    @Headers("Content-Type: application/json")
    @DELETE("/projWorkLogManage/WorkDLImage/{cons_code}/{sys_doc_num}/{file_index}")
    fun requestDeleteGallery(
        @Path("cons_code") cons_code : String,
        @Path("sys_doc_num") sys_doc_num: String,
        @Path("file_index") file_index : String,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<PostGalleryDTO>
}