package cloud.bjx.mm.android.oss

import cloud.bjx.mm.android.utils.LogUtil
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.common.OSSConstants
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken
import com.alibaba.sdk.android.oss.common.utils.IOUtils
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class OssAuthTokenProvider(private val authServerUrl: String?, private val mPolicyType: String) :
    OSSAuthCredentialsProvider(authServerUrl) {

    private var mDecoder: AuthDecoder? = null

    override fun getFederationToken(): OSSFederationToken {
        var authData: String?
        return try {
            val stsUrl = URL(authServerUrl)
            val conn = stsUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "POST";
            conn.setRequestProperty("Authorization", "Bearer ") // 暂时传空
            conn.connectTimeout = 10000
            conn.doOutput = true // 是否输入参数
            val bytes = "policyType=${mPolicyType}".encodeToByteArray()
            conn.outputStream.write(bytes) // 输入参数

            val input = conn.inputStream
            authData = IOUtils.readStreamAsString(input, OSSConstants.DEFAULT_CHARSET_NAME)
            if (mDecoder != null) {
                authData = mDecoder?.decode(authData)
            }
            val jsonObj = JSONObject(authData)
            val statusCode = jsonObj.getInt("code")

            val jsonDataObj = jsonObj.getJSONObject("data")
            val credentialsObj =
                jsonDataObj.getJSONObject("stsResponse").getJSONObject("Credentials")
            if (statusCode == 0) {
                LogUtil.i("fetch buildStsToken: $authData")
                val ak = credentialsObj.getString("AccessKeyId")
                val sk = credentialsObj.getString("AccessKeySecret")
                val token = credentialsObj.getString("SecurityToken")
                val expiration = credentialsObj.getString("Expiration")
                return OSSFederationToken(ak, sk, token, expiration)
            } else {
                val errorCode = credentialsObj.getString("ErrorCode")
                val errorMessage = credentialsObj.getString("ErrorMessage")
                throw ClientException("ErrorCode: $errorCode| ErrorMessage: $errorMessage")
            }

        } catch (e: Exception) {
            throw ClientException(e)
        }
    }

    /**
     * set response data decoder
     *
     * @param decoder
     */
    override fun setDecoder(decoder: AuthDecoder?) {
        this.mDecoder = decoder
    }

}