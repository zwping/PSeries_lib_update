package win.zwping.update

import android.content.Intent
import android.os.IBinder
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import win.zwping.code.basic.lifecycle.BasicLifeCycleService
import win.zwping.frame.RxBusUtil
import win.zwping.frame.http.Http
import java.io.File

class DownApkService : BasicLifeCycleService() {

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        down(intent?.getStringExtra("url"))
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Http.destroy(this)
        super.onDestroy()
        stopSelf()
    }

    private fun down(url: String?) {
        if (url == null) {
            RxBusUtil.post(1, "errorDown")
            return
        }
        OkGo.get<File>(url)
            .execute(object : FileCallback() {
                override fun onSuccess(response: Response<File>?) {
                    // println(response?.body()?.path)
                    RxBusUtil.post(response?.body()?.path,"sucDown")
                }

                override fun onError(response: Response<File>?) {
                    super.onError(response)
                    RxBusUtil.post(1, "errorDown")
                }

                override fun onStart(request: Request<File, out Request<Any, Request<*, *>>>?) {
                    super.onStart(request)
                    RxBusUtil.post(1, "startDown")
                }

                override fun downloadProgress(progress: Progress?) {
                    super.downloadProgress(progress)
                    progress?.also {
                        // println(((it.currentSize.toFloat() / it.totalSize) * 100).toInt())
                        RxBusUtil.post(((it.currentSize.toFloat() / it.totalSize) * 100).toInt(), "ingDown")
                    }
                }

            })
    }

}