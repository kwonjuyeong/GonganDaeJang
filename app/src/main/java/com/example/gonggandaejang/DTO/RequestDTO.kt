package com.allscapeservice.a22allscape_app.DTO


//사용자 로그인
data class LoginRequestDTO(
    val userId: String,
    val passwd: String
)
//작업일지 댓글 등록
data class ReplyPostRequestDTO(
    val content : String
)

//작업일지 댓글 수정
data class ReplyPutRequestDTO(
    val content : String
)

//회사 리스트 제공(수정 GET -> POST, @PATH -> @BODY)
data class GetCoListRequestDTO(
    val coName : String
)

data class ModifyRequestDTo(
    val id : String,
    val authority_code : String,
    val authority_name : String,
    val password : String,
    val user_state : String,
    val user_state_name : String,
    val use_type : String,
    val user_name : String,
    val user_position : String,
    val user_contact : String,
    val user_email : String,
    val co_address : String,
    val co_ceo : String,
    val co_contact : String,
    val co_name : String,
    val co_regisnum : String,
    val co_type : String
)