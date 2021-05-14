package cloud.bjx.mm.android.oss

import cloud.bjx.mm.android.ContextProvider
import cloud.bjx.mm.android.bean.BucketBean
import cloud.bjx.mm.android.reqUrl
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.OSS
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.OSSLog

object OssClient {

    fun initOSSClient(bucket: BucketBean?, mPolicyType: String): OSS {
        val credentialProvider =
            OssAuthTokenProvider("${reqUrl}/api/v1/sspanel/oss/buildStsToken", mPolicyType)
        val conf = ClientConfiguration()
        conf.connectionTimeout = 15 * 1000 // 连接超时，默认15秒
        conf.socketTimeout = 15 * 1000 // socket超时，默认15秒
        conf.maxConcurrentRequest = 5 // 最大并发请求书，默认5个
        conf.maxErrorRetry = 2 // 失败后最大重试次数，默认2次
        val oss: OSS = OSSClient(ContextProvider.get(), bucket?.endpoint, credentialProvider, conf)
        OSSLog.disableLog()
        return oss
    }
}
