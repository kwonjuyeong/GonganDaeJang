package com.gonggan.DTO

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




