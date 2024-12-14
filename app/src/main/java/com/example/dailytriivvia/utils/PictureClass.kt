package com.example.dailytriivvia.utils


import android.util.Log
import com.example.dailytriivvia.models.Picture
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PictureClass {

    companion object {
        private const val PEXELS_API_KEY = "ua4KGCx5nkAiTe2R2sjkwPg4SOniQMPB6MFPOwBpPTek2ZvkgsKfwPdA"
        private const val PEXELS_API_URL = "https://api.pexels.com/v1/search?query="
    }

    interface DataCallback {
        fun onSuccess(picture: Picture)
        fun onFailure(errorMessage: String)
    }

    private val client = OkHttpClient()

    fun fetchImage(keyword: String, callback: DataCallback) {
        val request = Request.Builder()
            .url("$PEXELS_API_URL$keyword")
            .addHeader("Authorization", PEXELS_API_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Request Failed", e)
                callback.onFailure("Request Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val jsonResponse = JSONObject(response.body!!.string())
                        val photos = jsonResponse.getJSONArray("photos")
                        if (photos.length() > 0) {
                            val imageUrl = photos.getJSONObject(0)
                                .getJSONObject("src")
                                .getString("medium")
                            callback.onSuccess(Picture(imageUrl))
                        } else {
                            callback.onFailure("No images found.")
                        }
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Parsing Error", e)
                        callback.onFailure("Parsing Error: ${e.message}")
                    }
                } else {
                    callback.onFailure("Response not successful")
                }
            }
        })
    }
}
