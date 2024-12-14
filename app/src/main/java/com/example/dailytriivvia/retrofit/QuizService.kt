package com.example.dailytriivvia.retrofit


import com.example.dailytriivvia.models.QuizResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface QuizService {
    @GET("api.php")
    fun getQuiz(
        @Query("amount")amount:Int,
        @Query("category")category:Int?,
        @Query("difficulty")difficulty:String?,
        @Query("type")type:String?
    ): Call<QuizResponse>
}

