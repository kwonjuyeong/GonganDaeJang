package com.allscapeservice.a22allscape_app.DTO


//3.2.1 사용자 로그인
data class LoginRequestDTO(
    val userId: String,
    val passwd: String
)
//3.2.1 사용자 로그인
data class ReplyPostRequestDTO(
    val content : String
)
