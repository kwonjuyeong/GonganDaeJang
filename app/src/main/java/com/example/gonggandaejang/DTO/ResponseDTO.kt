package com.allscapeservice.a22allscape_app.DTO


//사용자 로그인
data class LoginDTO(
    val code : Int,
    val msg : String,
    val value : Login
)
data class Login(
    val token : String,
    val state : String
)

//사용자 로그아웃
data class LogoutDTO(
    val code : Int,
    val msg : String,
    val value : String
)

//사진첩 불러오기
data class GetGallery(
    val code : Int,
    val msg  : String,
    val value : ArrayList<Gallerys>
)
data class Gallerys(
    val cons_code : String,
    val image_title : String,
    val image_path : String,
    val image_orig_name : String,
    val image_chan_name : String,
    val upload_date : String
)

//날짜 및 시간 제공
data class GetCurTimeInfoDTO(
    val code : Int,
    val msg : String,
    val value : String
)

//코드 리스트 제공
data class GetCodeListDTO(
    val code : Int,
    val msg : String,
    val value : ArrayList<CodeList1>
)
data class CodeList1(
    val fullcode : String,
    val subcode_name : String
)


//3.3.22 프로젝트 리스트 조회(Project Go)
data class ProjectGoDTO(
    val code : Int,
    val msg : String,
    val value : ArrayList<ProjectGo>
)
data class ProjectGo(
    val add_info : String,
    val building_area : String,
    val building_name : String,
    val cons_code : String,
    val cons_name : String,
    val cons_type : String,
    val cons_type_name : String,
    val design_price : String,
    val floor_area : String,
    val go_price : String,
    val ground : String,
    val households : String,
    val location : String,
    val location_contact : String,
    val main_building : String,
    val project_status : String,
    val project_status_name : String,
    val purpose : String,
    val purpose_name: String,
    val site_area : String,
    val structure : String,
    val sub_building : String,
    val total_area : String,
    val underground : String,
    val project_progress : String
)


//3.2.3 내 정보 요청
data class UserInfoDTO(
    val code : Int,
    val msg : String,
    val value : UserInfo
)
data class UserInfo(
    val app_token : String,
    //승인날짜
    val appro_date : String,
    val authority_code : String,
    val authority_name : String,
    val bs_license_change_name : String,
    val bs_license_original_name : String,
    val bs_license_path : String,
    val co_address : String,
    val co_ceo : String,
    val co_code : String,
    val co_contact : String,
    val co_license_change_name : String,
    val co_license_original_name : String,
    val co_license_path : String,
    val co_name : String,
    val co_regisnum : String,
    val co_type : String,
    val employ_status : String,
    val employ_status_name : String,
    val field_rating : ArrayList<FieldRating>,
    val id : String,
    val join_date : String,
    val password : String,
    val regisnum : String,
    val sign_change_name : String,
    val sign_original_name : String,
    val sign_path : String,
    val use_type : String,
    val user_contact : String,
    val user_email : String,
    val user_license_change_name : String,
    val user_license_original_name: String,
    val user_license_path : String,
    val user_name : String,
    val user_position : String,
    val user_regisnum : String,
    val user_state : String,
    val user_state_name : String,
    val user_type : String,
    val user_type_name : String,
    val web_token : String,
    val manager_type : String
)
data class FieldRating(
    val field : String,
    val field_name : String,
    val id : String,
    val rating : String,
    val rating_name : String
)

//프로젝트 상태 통계현황 조회
data class ProjectListDTO(
    val code : Int,
    val msg : String,
    val value : ArrayList<ProjectList>
)
data class ProjectList(
    val cnt : Int,
    val proj_status_cd : String,
    val proj_status_nm : String,
    val reside_class_cd : String,
    val reside_class_nm : String
)

//작업일지 리스트 조회
data class WorkListResponseDTO(
    val code : Int,
    val msg : String,
    val value : WorkList
)
data class WorkList(
    val count : Int,
    val data : ArrayList<WorkLIstData>
)
data class WorkLIstData(
    val cons_date : String,
    val id : String,
    val work_title : String,
    val write_date : String,
    val sys_doc_num : String
)

//사진대지
data class PostGalleryDTO(
    val code : Int,
    val msg : String,
    val value : String
)

//작업일지 상세 조회
data class DailyWorkDTO(
    val code : Int,
    val msg : String,
    val value : DailyWorkD
)
data class DailyWorkD(
    val cons_code : String,
    val cons_date : String,
    val cons_name : String,
    val work_diary : String,
    val cons_content_info : ConsContentInfo,
    val cons_manp_info : ArrayList<ConsManpInfo>,
    val cons_work_info : ArrayList<ConsWorkInfo>
)
data class ConsContentInfo(
    val next_content : String,
    val today_content : String
)
data class ConsManpInfo(
    val cons_type_cd : String,
    val cons_type_explain : String,
    val cons_type_nm : String,
    val level1_name : String,
    val level2_name : String,
    val level3_name : String,
    val next_manpower : Int,
    val prev_manpower : Int,
    val product : String,
    val today_manpower : Int,
    val work_log_cons_code : String,
    val work_log_cons_lv1 : Int,
    val work_log_cons_lv2 : Int,
    val work_log_cons_lv3 : Int,
    val work_log_cons_lv4 : Int
)
data class ConsWorkInfo(
    val cons_type_cd : String,
    val cons_type_explain : String,
    val cons_type_nm : String,
    val level1_name : String,
    val level2_name : String,
    val level3_name : String,
    val next_workload : Int,
    val prev_workload : Int,
    val product : String,
    val quantity : Float,
    val today_workload : Int,
    val total_workload : Int,
    val unit : String,
    val work_log_cons_code : String,
    val work_log_cons_lv1 : Int,
    val work_log_cons_lv2 : Int,
    val work_log_cons_lv3 : Int,
    val work_log_cons_lv4 : Int,
    val imageList : ArrayList<ImageData>
)
data class ImageData(
    val change_name : String,
    val cons_date : String,
    val cons_type_cd : String,
    val cons_type_explain : String,
    val cons_type_nm : String,
    val file_index : Int,
    val origin_name : String,
    val path : String,
    val title : String,
    val upload_date : String
)

