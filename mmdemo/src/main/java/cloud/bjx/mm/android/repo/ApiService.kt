package cloud.bjx.mm.android.repo

import cloud.bjx.mm.android.bean.OssBean
import cloud.bjx.mm.android.bean.ResultBean
import cloud.bjx.mm.android.bean.TokenBean
import cloud.bjx.mm.android.reqUrl
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    /** 获取 appKey、token */
    @GET("api/v1/appserver/token")
    suspend fun fetchToken(@Query("userID") uid: String): ResultBean<TokenBean>

    @Multipart
    @POST("api/v1/appserver/token")
    suspend fun uploadVideoFile(
        @Query("userID") uid: String,
        @Part("api_key") api_key: RequestBody,
        @Part file: MultipartBody.Part
    ): ResultBean<TokenBean>


    /** 获取OSS bucket信息 */
    @FormUrlEncoded
    @POST("api/v1/sspanel/oss/bucketInfo")
    suspend fun fetchBucketInfo(
        @Field("policyType") policyType: String
    ): ResultBean<OssBean>

}

object ServiceFactory {
    // private const val url = "http://srtcauth.bjx.cloud" // sit 地址

    // private const val reqUrl = "http://192.168.88.222:8888" // 内网地址

    val apiService: ApiService by lazy {
        RetrofitFactory.retrofit(reqUrl).create(ApiService::class.java)
    }

}