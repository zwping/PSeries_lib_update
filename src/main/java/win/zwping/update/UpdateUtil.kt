package win.zwping.update

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.yanzhenjie.permission.AndPermission
import win.zwping.code.comm.Bean
import win.zwping.code.review.PProgressBar
import win.zwping.code.review.PTextView
import win.zwping.code.utils.AcUtil
import win.zwping.code.utils.AppUtil
import win.zwping.code.utils.ConversionUtil.covInteger
import win.zwping.code.utils.EmptyUtil.isNotEmpty
import win.zwping.code.utils.EnUtil.covBundle
import win.zwping.code.utils.ToastUtil
import win.zwping.code.utils.ViewUtil
import win.zwping.frame.comm.CommPop

object UpdateUtil {

    fun forceUpdate(
        oldVersionName: String?,
        newVersionName: String?, // 需要在后台准确定义版本名称
        describe: String?,
//        force: Boolean, // 目前只做强制升级单通道
        downUrl: String?,
        browserUpdateUrl: String?,
        callBack: UpdateCallBack?
    ) {
        if (isNotEmpty(newVersionName) &&
            covInteger(newVersionName?.replace(".", "")) >
            covInteger(oldVersionName?.replace(".", ""))
        ) {
            AcUtil.startActivity(
                covBundle(
                    Bean("newVersionName", newVersionName), Bean("describe", describe),
                    Bean("url", downUrl), Bean("browserUpdateUrl", browserUpdateUrl)
                ),
                UpdateAc::class.java
            )
            UpdateAc.setCallBack(callBack)
        } else callBack?.normal()
    }


}