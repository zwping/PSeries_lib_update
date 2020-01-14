package win.zwping.update

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import com.yanzhenjie.permission.runtime.Permission.READ_EXTERNAL_STORAGE
import com.yanzhenjie.permission.runtime.Permission.WRITE_EXTERNAL_STORAGE
import win.zwping.code.basic.BasicAc
import win.zwping.code.review.PProgressBar
import win.zwping.code.utils.AnimatorUtil
import win.zwping.code.utils.IntentUtil
import win.zwping.frame.RxBusUtil
import win.zwping.frame.comm.CommPop
import java.io.File


/**
 * describe：透明activity
 *     note：
 *  @author：zwp on 2019-08-13 mail：1101558280@qq.com web: http://www.zwping.win
 */
class UpdateAc : BasicAc() {

    companion object {
        private var callBack: UpdateCallBack? = null

        fun setCallBack(callBack: UpdateCallBack?) {
            this.callBack = callBack
        }
    }

    private val pbar: PProgressBar by lazy {
        PProgressBar(this, null, android.R.style.Widget_ProgressBar_Horizontal).also {
            it.setHorizontalParams(Color.parseColor("#33c9c9c9"), Color.parseColor("#ccc9c9c9"))
            it.layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private val pop: CommPop by lazy {
        CommPop(this).also {
            it.setOutsideDismiss().setCancelHide().setBackPressDismiss()
                .setTitle("发现新版本 (${intent?.getStringExtra("newVersionName")})")
                .setContent("${intent?.getStringExtra("describe")}").setHandleView { it?.findViewById<TextView>(R.id.content_ptv)?.gravity = Gravity.START }
                .setConfirmTxt("确认升级")
        }
    }

    override fun bindLayout() = android.R.layout.list_content

    override fun initView(savedInstanceState: Bundle?) {
        imBar.init()

        RxBusUtil.subscribeI(this, "startDown") {
            pop.findViewById<View>(R.id.confirm_ptv)?.isEnabled = false
            pop.setConfirmTxt("下载中...").setCancelHide()
            pbar.visibility = View.INVISIBLE
            pop.findViewById<ViewGroup>(R.id.bottom_ly)?.addView(pbar)
            pbar.post {
                AnimatorUtil.translationX(pbar, -pbar.width.toFloat(), 0F, 500)
                pbar.visibility = View.VISIBLE
            }
        }
        RxBusUtil.subscribeI(this, "ingDown") { pbar.progress = it }
        RxBusUtil.subscribeS(this, "sucDown") {
            pop.findViewById<ViewGroup>(R.id.bottom_ly)?.removeView(pbar)
            pop.findViewById<View>(R.id.confirm_ptv)?.isEnabled = true
            pop.setConfirmTxt("立即安装")
                .setConfirmLis { pop, s -> permissionInstall(it) }
            permissionInstall(it)
        }
        RxBusUtil.subscribeI(this, "errorDown") {
            showToast("更新失败 !!")
            pop.setConfirmTxt("确认升级")
            pop.findViewById<View>(R.id.confirm_ptv)?.isEnabled = true
            pop.findViewById<ViewGroup>(R.id.bottom_ly)?.removeView(pbar)
            pop.setCancelHide(false).setCancelTxt("前往官网更新").setCancelLis {
                startActivity(IntentUtil.openDefaultBrowser(intent?.getStringExtra("browserUpdateUrl")))
            }
        }
    }

    override fun doBusiness() {
        pop.setConfirmLis { pop, s -> permission() }.show()
    }

    private fun permission() {
        AndPermission.with(this).runtime().permission(Permission.Group.STORAGE)
            .onGranted {
                startService(Intent(this, DownApkService::class.java).also {
                    it.putExtra("url", intent?.getStringExtra("url"))
                })
            }.onDenied {
                if (AndPermission.hasAlwaysDeniedPermission(this, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))
                    CommPop(this).setBackPressDismiss().setOutsideDismiss().setContent("权限被拒绝，是否前往手机设置中开启存储权限")
                        .setConfirmTxt("前往设置").setConfirmLis { pop, s ->
                            pop.dismiss();AndPermission.with(this).runtime().setting().start(1999)
                        }.setCancelLis { finish();callBack?.finishApp() }.setCancelTxt("关闭APP").show()
                else
                    CommPop(this).setBackPressDismiss().setOutsideDismiss().setCancelHide().setContent("存储权限是APP升级必备权限，请重新开启")
                        .setConfirmTxt("重新开启").setConfirmLis { pop, s ->
                            pop.dismiss();permission()
                        }.show()
            }.start()
    }

    private fun permissionInstall(path: String) {
        AndPermission.with(this).install().file(File(path)).onGranted { }.onDenied { }.start()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1999) permission()
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBusUtil.unregister(this)
    }

}