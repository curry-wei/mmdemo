package cloud.bjx.mm.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import cloud.bjx.mm.android.bean.BucketBean
import cloud.bjx.mm.android.oss.OssClient
import cloud.bjx.mm.android.repo.ServiceFactory
import cloud.bjx.mm.android.utils.LogUtil
import cloud.bjx.mm.android.utils.Outcome
import cloud.bjx.mm.android.utils.ProgressHUD
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSS
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.model.OSSRequest
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import java.io.File
import java.util.*

class UploadFileViewModel : ViewModel() {

    val uploadResult = MutableLiveData<Boolean>()

    fun uploadFile(policyType: String = "meetingVideo", savePath: String) =
        liveData {

            val result = ServiceFactory.apiService.fetchBucketInfo(policyType)
            if (result.isSuccess()) {
                emit(Outcome.Success(result))
                val bucket = result.data.bucket
                val oss = OssClient.initOSSClient(bucket, cloud.bjx.mm.android.policyType)
                uploadOssImage(bucket, oss, savePath)
            } else {
                LogUtil.e("fetch ailyn oss bucket request fail: $result")
                uploadResult.value = false
                emit(Outcome.Failure(result.message))
            }
        }


    private fun uploadOssImage(bucket: BucketBean, oss: OSS, savePath: String) {

        val file = File(savePath)
        if (!file.exists()) {
            LogUtil.w("asyncPutImage FileNotExist")
            return
        }

        val objectName =
            bucket.prefix + UUID.randomUUID().toString() + ".mp4"
        // 构造上传请求
        val put = PutObjectRequest(bucket.name, objectName, savePath)
        put.crC64 = OSSRequest.CRC64Config.YES

        // 异步上传时可以设置进度回调
        put.progressCallback = OSSProgressCallback { _, currentSize, totalSize ->
            val progress = (100 * currentSize / totalSize).toInt()
            if (progress == 100) {
                LogUtil.i("image currentSize: $currentSize totalSize: $totalSize progress:$progress")
                ProgressHUD.dismiss()
            }
        }

        oss.asyncPutObject(
            put,
            object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                override fun onSuccess(request: PutObjectRequest, result: PutObjectResult) {
                    LogUtil.i("upload oss success server url: ${Thread.currentThread().name}")
                    val ossImgUrl = "${bucket.domain}/${objectName}"
                    //全部上传完成
                    ProgressHUD.dismiss()
                    LogUtil.i("upload oss success server url: $ossImgUrl")
                    uploadResult.value = true
                }

                override fun onFailure(
                    request: PutObjectRequest,
                    clientExcepion: ClientException,
                    serviceException: ServiceException
                ) {
                    uploadResult.value = false
                    val info = serviceException.toString()
                    LogUtil.e("upload oss fail serviceException: $info,clientException: $clientExcepion")
                }
            })
    }

}