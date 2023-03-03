package com.gonggan.objects

//년"월"일 1~9 앞에 0 붙여주는 함수 ex) 1, 2, 3, 4 -> 01, 02, 03, 04
fun getMonth(month: Int): String {
    return if (month in 0..8) {
        val monthReal = month+1
        "0$monthReal"
    } else {
        val monthReal = month+1
        monthReal.toString()
    }
}

//년월"일" 1~9 앞에 0 붙여주는 함수 ex) 1, 2, 3, 4 -> 01, 02, 03, 04
fun getDay(day: Int): String {
    return if (day in 1..9) {
        "0${day}"
    } else {
        day.toString()
    }
}

//시작 날짜 ex)20221026 + 000000 = 20221026000000
fun startDate(date :String?) : String {
    return if(date == ""){
        ""
    } else{
        val result : String
        val year : String = date!!.substring(0, 4)
        val month : String = date.substring(5, 7)
        val day : String = date.substring(8, 10)
        result = year+month+day+"000000"
        result
    }
}

//종료 날짜 ex)20221026 + 595959 = 20221026595959
fun endDate(date :String?) : String {
    return if(date == ""){
        ""
    } else{
        val result : String
        val year : String = date!!.substring(0, 4)
        val month : String = date.substring(5, 7)
        val day : String = date.substring(8, 10)
        result = year+month+day+"595959"
        result
    }
}

//시작 날짜 ex)20221026 + 000000 = 20221026000000
fun postSearchStartDate(date :String?) : String {
    return if(date == ""){
        ""
    } else{
        val result : String
        val year : String = date!!.substring(0, 4)
        val month : String = date.substring(5, 7)
        val day : String = date.substring(8, 10)
        val convertDay : String = (day.toInt() - 3).toString()
        result = year+month+convertDay+"000000"
        result
    }
}

//날짜 포맷 바꾸기 ex) 20221026 -> 2022-10-26
fun convertDateFormat(date :String?) : String {
    return if(date?.length!! < 6){
        ""
    } else{
        val result : String
        val year : String = date.substring(0, 4)
        val month : String = date.substring(4, 6)
        val day : String = date.substring(6, 8)
        result = "$year-$month-$day"
        result
    }
}

//날짜 포맷 바꾸기 ex) 20221026000000 -> 2022년10월26일
fun convertDateFormat2(date :String?) : String {
    return if(date == ""){
        ""
    } else{
        val result : String
        val year : String = date!!.substring(0, 4)
        val month : String = date.substring(4, 6)
        val day : String = date.substring(6, 8)
        result = "${year}년${month}월${day}일"
        result
    }
}


//날짜 포맷 바꾸기 ex) 2022-10-26 14:46:19 -> 20221026144619
fun convertDateFormat3(date :String?) : String {
    return if(date == ""){
        ""
    } else{
        val result : String
        val year : String = date!!.substring(0, 4)
        val month : String = date.substring(5, 7)
        val day : String = date.substring(8, 10)
        val hour : String = date.substring(11,13)
        val minuit : String = date.substring(14, 16)
        val second : String = date.substring(17,19)
        result = "${year}${month}${day}${hour}${minuit}${second}"
        result
    }
}

//날짜 포맷 바꾸기 ex) 20221026144619 -> 2022-10-26 14:46:19
fun convertDateFormat4(date :String?) : String {
    return if(date == ""){
        ""
    } else{
        val result : String
        val year : String = date!!.substring(0, 4)
        val month : String = date.substring(4, 6)
        val day : String = date.substring(6, 8)
        val hour : String = date.substring(8,10)
        val minuit : String = date.substring(10, 12)
        val second : String = date.substring(12,14)
        result = "${year}-${month}-${day}\n${hour}:${minuit}:${second}"
        result
    }
}

//날짜 포맷 바꾸기 ex) 20221026 -> 2022.10.26
fun convertDateFormat5(date :String?) : String {
    return if(date?.length!! < 6){
        ""
    } else{
        val result : String
        val year : String = date.substring(0, 4)
        val month : String = date.substring(4, 6)
        val day : String = date.substring(6, 8)
        result = "$year.$month.$day"
        result
    }
}