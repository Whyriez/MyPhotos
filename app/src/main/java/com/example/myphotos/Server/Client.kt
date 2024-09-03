package com.example.myphotos.Server

import android.app.Activity
import android.widget.Toast

object Client {
    val baseUrl ="http://192.168.1.5:5143"

    var idUser = ""

    fun message(message: String, activity: Activity){
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
//        val alertDialog = AlertDialog.Builder(activity)
//            .setTitle("Message")
//            .setMessage(message)
//            .setCancelable(false)
//            .setPositiveButton("OK"){s, t->
//                if(st){
//                    activity.finish()
//                }
//            }
//        alertDialog.show()
    }
}