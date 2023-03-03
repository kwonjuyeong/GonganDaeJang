package com.gonggan.objects

import java.security.MessageDigest

fun getSHA512(input:String):String{
    return MessageDigest
        .getInstance("SHA-512")
        .digest(input.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

/*
//비밀번호 암호화 모듈(SHA-512)
fun getSHA512(input: String): String {
    val md: MessageDigest = MessageDigest.getInstance("SHA-512")
    val messageDigest = md.digest(input.toByteArray())
    val no = BigInteger(1, messageDigest)
    var hashText: String = no.toString(16)

    while (hashText.length < 32) {
        hashText = "0$hashText"
    }
    return hashText
}*/