package com.example.dailytriivvia.utils

import android.content.Context
import android.provider.SyncStateContract
import android.widget.Toast

object Utils {

    fun showToast(context: Context, msg:String){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
    }
}