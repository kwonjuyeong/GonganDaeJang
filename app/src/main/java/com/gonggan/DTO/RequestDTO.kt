package com.gonggan.DTO

import java.io.File

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

data class OutsideDivision(
    var parentTitle : String,
    val fileList : ArrayList<Images>
)

data class Images(
    var file_index: Int,
    val origin_name: String,
    val file_path : String,
    var title : String,
    val file : File?
)


data class ModifyGalleryDTO(
    val title : String
)

data class ConvertedImg(
    val title : String,
    val origName : String,
    val file : File?,
    val file_index: Int
)