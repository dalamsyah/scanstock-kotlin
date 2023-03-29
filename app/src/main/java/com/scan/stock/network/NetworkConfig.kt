package com.scan.stock.network

import com.scan.stock.model.ResultScanStock
import com.scan.stock.model.ScanStock
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

class NetworkConfig {
    // set interceptor
    fun getInterceptor() : OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return  okHttpClient
    }
    fun getRetrofit(url: String) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(getInterceptor())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun getService(url: String) = getRetrofit(url).create(ScanStockInterface::class.java)
}
interface ScanStockInterface {

    @Headers("Content-Type: application/json")
    @POST("api.php")
    fun post(@Body body: Map<String, String?>): Call<ResultScanStock>

}