package com.gonggan.API
//정보 제공 인터페이스
import com.gonggan.DTO.*
import com.gonggan.DTO.LoginDTO
import com.gonggan.DTO.LoginRequestDTO
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

interface GetWeatherService {
    @Headers("Content-Type: application/json")
    @GET("/commManage/getWeatherInfo")
    fun requestWeather(
        @Header("sysCd") sysCd: String?,
        @Header("token") token: String?,
    ): Call<GetWeatherInfoDTO>
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
    @GET("/projStatistManage/ProjStatusStatistics")
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
        @Path("file_index") file_index : Int,
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
    @DELETE("/projWorkLogManage/WorkDLImage/{cons_code}/{sys_doc_num}/{work_log_cons_code}/{file_index}")
    fun requestDeleteGallery(
        @Path("cons_code") cons_code : String,
        @Path("sys_doc_num") sys_doc_num: String,
        @Path("work_log_cons_code") work_log_cons_code : String,
        @Path("file_index") file_index : Int,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<PostGalleryDTO>
}

//공사일보 댓글 관리 =======================================================================================
//공사일보 댓글 조회
interface GetReply{
    @Headers("Content-Type: application/json")
    @GET("/projWorkReplyManage/WorkReply/{sys_doc_num}")
    fun requestGetReply(
        @Path("sys_doc_num") sys_doc_num: String,
        @Query("parent_uuid") parent_uuid : String,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<ReplyDTO>
}

//공사일보 댓글 삭제
interface DeleteReply{
    @Headers("Content-Type: application/json")
    @DELETE("/projWorkReplyManage/WorkReply/{sys_doc_num}")
    fun requestDeleteReply(
        @Path("sys_doc_num") sys_doc_num: String,
        @Query("uuid") uuid : String,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<PostGalleryDTO>
}

//공사일보 댓글 작성
interface PostReply{
    @Headers("Content-Type: application/json")
    @POST("/projWorkReplyManage/WorkReply/{sys_doc_num}")
    fun requestPostReply(
        @Path("sys_doc_num") sys_doc_num: String,
        @Query("parent_uuid") parent_uuid: String,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String,
        @Body ReplyPostRequestDTO : ReplyPostRequestDTO
    ): Call<PostGalleryDTO>
}

//공사일보 댓글 수정
interface PutReply{
    @Headers("Content-Type: application/json")
    @PUT("/projWorkReplyManage/WorkReply/{sys_doc_num}")
    fun requestPutReply(
        @Path("sys_doc_num") sys_doc_num: String,
        @Query("uuid") uuid: String,
        @Header("sysCd") sysCd: String,
        @Header("token") token : String,
        @Body ReplyPutRequestDTO : ReplyPutRequestDTO
    ): Call<ReplyDTO>
}

//회사 리스트 제공(수정 GET -> POST, @PATH -> @BODY)
interface GetCoListService {
    @Headers("Content-Type: application/json")
    @POST("/commManage/getCoList")
    fun requestGetCoList(
        @Header("sysCd") sysCd: String,
        @Body coName : GetCoListRequestDTO
    ): Call<GetCoListDTO>
}

interface ModifyUserService {
    @Multipart
    @POST("/userManage/modifyUser")
    fun requestModify(
        @Header("sysCd") sysCd: String?,
        @Header("token") token: String?,
        @Part("data") data : RequestBody,
    ): Call<ModifyUserDTO>
}

//3.14.1 회사 재직 이력 정보조회
interface GetCoHistory{
    @Headers("Content-Type: application/json")
    @GET("/historyManage/getCoHisList")
    fun requestGetCoHistory(
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<CoHistoryDTO>
}

//3.14.2 프로젝트 이력 정보조회
interface GetProjHistory{
    @Headers("Content-Type: application/json")
    @GET("/historyManage/getProjHisList")
    fun requestGetProjHistory(
        @Header("sysCd") sysCd: String,
        @Header("token") token : String
    ): Call<ProjHistoryDTO>
}
