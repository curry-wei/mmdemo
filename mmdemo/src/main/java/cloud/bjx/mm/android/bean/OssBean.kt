package cloud.bjx.mm.android.bean

import androidx.annotation.Keep

@Keep
data class OssBean(
    val bucket: BucketBean,
    val stsResponse: StsResponseBean
)

@Keep
data class BucketBean(
    val domain: String,
    val endpoint: String,
    val maxFileSize: Int,
    val name: String,
    val prefix: String,
    val suffix: String,
    val tokenExpireTime: Int
)

@Keep
data class StsResponseBean(
    val AssumedRoleUser: AssumedRoleUserBean,
    val Credentials: CredentialsBean,
    val RequestId: String
)

@Keep
data class AssumedRoleUserBean(
    val Arn: String,
    val AssumedRoleId: String
)

@Keep
data class CredentialsBean(
    val AccessKeyId: String,
    val AccessKeySecret: String,
    val Expiration: String,
    val SecurityToken: String
)