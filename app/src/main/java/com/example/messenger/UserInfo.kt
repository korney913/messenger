package com.example.messenger

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
@RequiresApi(Build.VERSION_CODES.O)

class User (){
    val id:Int=0
    var name:String = ""
    //var photo
    var dateOfDirth: LocalDate= LocalDate.now()
    var location:String=""
    var friendsId: List <Int> =  emptyList<Int>()
}