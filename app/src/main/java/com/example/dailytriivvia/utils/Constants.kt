package com.example.dailytriivvia.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.text.HtmlCompat

object Constants {

    fun isNetworkAvailable(context: Context) :Boolean{

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val network = connectivityManager.activeNetwork?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network)?: return false

        return when{
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
            else-> false
        }

    }

    fun getRandomOptions(correctAnswer:String, incorrectAnswer:List<String>):Pair<String,List<String>>{
        val list = mutableListOf<String>()
        list.add(decodeHtmlString(correctAnswer))
        for (i in incorrectAnswer){
            list.add(decodeHtmlString(i))
        }

        list.shuffle()
        return Pair(correctAnswer,list)
    }

    fun decodeHtmlString(htmlEncoded:String):String{

        return HtmlCompat.fromHtml(htmlEncoded,HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }




}