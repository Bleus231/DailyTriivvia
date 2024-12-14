package com.example.dailytriivvia.utils

import android.content.Context
import android.content.Intent
import com.example.dailytriivvia.activities.QuizActivity
import com.example.dailytriivvia.models.QuizResponse
import com.example.dailytriivvia.retrofit.QuizService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuizClass (private val context: Context){

    fun getQuizList(amount:Int,category:Int?,difficulty:String?,type:String?){
        if (Constants.isNetworkAvailable(context)){
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create()).build()

            val service:QuizService = retrofit.create(QuizService::class.java)

            val dataCall: Call<QuizResponse> = service.getQuiz(amount, category, difficulty, type)

            dataCall.enqueue(object :Callback<QuizResponse>{
                override fun onResponse(
                    call: Call<QuizResponse>,
                    response: Response<QuizResponse>
                ) {

                    if (response.isSuccessful) {
                        val responseData:QuizResponse = response.body()!!
                        val  questionList = ArrayList(responseData.results)
                        if (questionList.isNotEmpty()){
                            val intent = Intent(context,QuizActivity::class.java)
                            intent.putExtra("questionList",questionList)
                            context.startActivity(intent)
                        }
                    } else {
                        Utils.showToast(context,"Response Failed")
                    }
                }

                override fun onFailure(call: Call<QuizResponse>, t: Throwable) {
                    Utils.showToast(context,"Failure in response")
                }

            })
        } else {
            Utils.showToast(context,"Network is not available")
        }
    }
}